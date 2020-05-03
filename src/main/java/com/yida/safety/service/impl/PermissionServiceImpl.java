package com.yida.safety.service.impl;

import com.yida.safety.common.BaseResponseCode;
import com.yida.safety.constants.Constant;
import com.yida.safety.dao.SysPermissionMapper;
import com.yida.safety.dao.SysRoleMapper;
import com.yida.safety.dao.SysRolePermissionMapper;
import com.yida.safety.exception.BusinessException;
import com.yida.safety.pojo.SysPermission;
import com.yida.safety.service.*;
import com.yida.safety.util.TokenSettings;
import com.yida.safety.vo.req.PermissionAddReqVO;
import com.yida.safety.vo.req.PermissionUpdateReqVO;
import com.yida.safety.vo.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-24 17:15
 **/
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TokenSettings tokenSettings;
    @Autowired
    private DeptRoleService deptRoleService;
    @Autowired
    private DeptUserService deptUserService;
    /**
     * @Description: 查询全部菜单
     */
    @Override
    public List<SysPermission> selectAll() {
        List<SysPermission> result = sysPermissionMapper.selectAll();
        if(!result.isEmpty()){
            for (SysPermission sysPermission:result){
                SysPermission parent=sysPermissionMapper.selectByPrimaryKey(sysPermission.getPid());
                if (parent!=null){
                    sysPermission.setPidName(parent.getName()); }
            } }
        return result;
    }
    /**
     * @Description:  获取权限树 前端选择模块
     */
    @Override
    public List<MenuTreeRespVO> selectAllTree() {
        return getTree5(selectAll());
    }
    /**
     * @Description: 获取用户菜单
     */
    @Override
    public List<MenuRespVO> getMenu(String userId) {
        List<SysPermission> list=getPermissionById(userId);
        return getTree(list,true);
    }
    /**
    * @Description: 获取权限
    */
    @Override
    public List<MenuRespVO> getPermission() {
        List<SysPermission> list=sysPermissionMapper.selectAll();
        return getTree(list,true);
    }
    /**
    * @Description: 查询权限列表
    */
    @Override
    public List<PermissionRespAllVO> selectPermissionTable() {
        List<SysPermission> list = selectAll();
        return getTree2(list,false);
    }
    
    /** 
    * @Description: 获取用户权限标识列表
    */ 
    @Override
    public List<String> getPermissionsByUserId(String userId) {

        List<SysPermission> list = getPermissionById(userId);
        List<String> permissions = new ArrayList<String>();
        if (null == list || list.isEmpty()){
            return null;
        }
        for (SysPermission sysPermission:list){
            if(!StringUtils.isEmpty(sysPermission.getPerms())){
                permissions.add(sysPermission.getPerms());
            }

        }
        return permissions;
    }
    /**
    * @Description: 添加权限
    */
    @Override
    public SysPermission addPermission(PermissionAddReqVO vo) {
        SysPermission sysPermission=new SysPermission();
        BeanUtils.copyProperties(vo,sysPermission);
        verifyForm(sysPermission);
        sysPermission.setId(UUID.randomUUID().toString());
        sysPermission.setCreateTime(new Date());
        int insert = sysPermissionMapper.insertSelective(sysPermission);
        if(insert!=1){
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        return sysPermission;
    }
    /**
    * @Description: 查询选择权限树
    */
    @Override
    public List<MenuTreeRespVO> selectMenuTree(String type) {
        List<SysPermission> list=sysPermissionMapper.selectAll();
        if (type.equals("false")){
            return getTree3(list,false);
        }if (type.equals("btn")){
            return getTree4(list);
        }
        return getTree3(list,true);
    }
    /**
     * @Description: 删除权限
     */
    @Override
    public void deletedPermission(String permissionId) {
        //判断是否有子集关联
        List<SysPermission> sysPermissions = sysPermissionMapper.selectChild(permissionId);
        if(!sysPermissions.isEmpty()){
            throw new BusinessException(BaseResponseCode.ROLE_PERMISSION_RELATION);
        }
        //解除相关角色和该菜单权限的关联
        rolePermissionService.removeRoleByPermissionId(permissionId);
        //更新权限数据
        SysPermission sysPermission=new SysPermission();
        sysPermission.setUpdateTime(new Date());
        sysPermission.setDeleted(0);
        sysPermission.setId(permissionId);
        int i = sysPermissionMapper.updateByPrimaryKeySelective(sysPermission);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //判断授权标识符是否发生了变化
        List<String> roleIdsByPermissionId = rolePermissionService.getRoleIdsByPermissionId(permissionId);
        if(!roleIdsByPermissionId.isEmpty()){
            List<String> userIdsByRoleIds = userRoleService.getUserIdsByRoleIds(roleIdsByPermissionId);
            if(!userIdsByRoleIds.isEmpty()){
                for (String userId:userIdsByRoleIds) {
                    redisService.set(Constant.JWT_REFRESH_KEY+userId,userId,tokenSettings.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                    /**
                     * 清楚用户授权数据缓存
                     */
                    redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
                }
            }
        }
    }
    /**
     * @Description: 修改权限
     */
    @Override
    public void updatePermission(PermissionUpdateReqVO vo) {
        //校验数据
        SysPermission update=new SysPermission();
        BeanUtils.copyProperties(vo,update);
        verifyForm(update);
        SysPermission sysPermission = sysPermissionMapper.selectByPrimaryKey(vo.getId());
        if(sysPermission==null){
            log.info("传入的id在数据库中不存在");
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        if(!sysPermission.getPid().equals(vo.getPid())){
            //所属菜单发生了变化要校验该权限是否存在子集
            List<SysPermission> sysPermissions = sysPermissionMapper.selectChild(vo.getId());
            if(!sysPermissions.isEmpty()){
                throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_UPDATE);
            }
        }

        update.setUpdateTime(new Date());
        int i = sysPermissionMapper.updateByPrimaryKeySelective(update);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }

        //判断授权标识符是否发生了变化
        if(!sysPermission.getPerms().equals(vo.getPerms())){
            List<String> roleIdsByPermissionId = rolePermissionService.getRoleIdsByPermissionId(vo.getId());
            if(!roleIdsByPermissionId.isEmpty()){
                List<String> userIdsByRoleIds = userRoleService.getUserIdsByRoleIds(roleIdsByPermissionId);
                if(!userIdsByRoleIds.isEmpty()){
                    for (String userId:userIdsByRoleIds) {
                        redisService.set(Constant.JWT_REFRESH_KEY+userId,userId,tokenSettings.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                        /**
                         * 清楚用户授权数据缓存
                         */
                        redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
                    }
                }
            }

        }
    }

    @Override
    public List<SysPermission> getPermissionsByIds(List<String> permissionIds) {
        List<SysPermission> sysPermissionList = new ArrayList<>();
        if (permissionIds.size() != 0){
            sysPermissionList = sysPermissionMapper.selectInfoByIds(permissionIds);
        }
        return sysPermissionList;
    }

    /**
     * - 操作后的菜单类型是目录的时候 父级必须为目录
     * - 操作后的菜单类型是菜单的时候，父类必须为目录类型
     * - 操作后的菜单类型是按钮的时候 父类必须为菜单类型
     */
    private void verifyForm(SysPermission sysPermission){

        SysPermission parent=sysPermissionMapper.selectByPrimaryKey(sysPermission.getPid());
        switch (sysPermission.getType()){
            case 1:
                if(parent!=null){
                    if(parent.getType()!=1){
                        throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_CATALOG_ERROR);
                    }
                }else if (!sysPermission.getPid().equals("0")){
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_CATALOG_ERROR);
                }
                break;
            case 2:
                if(parent==null||parent.getType()!=1){
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_MENU_ERROR);
                }
                if(StringUtils.isEmpty(sysPermission.getUrl())){
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_NOT_NULL);
                }
                break;
            case 3:
                if(parent==null||parent.getType()!=2){
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_BTN_ERROR);
                }
                if(StringUtils.isEmpty(sysPermission.getPerms())){
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_PERMS_NULL);
                }
                if(StringUtils.isEmpty(sysPermission.getUrl())){
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_NOT_NULL);
                }
                if(StringUtils.isEmpty(sysPermission.getMethod())){
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_METHOD_NULL);
                }
                if(StringUtils.isEmpty(sysPermission.getCode())){
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_CODE_NULL);
                }
                break;
        }
    }

    /**
    * @Description: 获取用户权限列表
    */
    public List<SysPermission> getPermissionById(String userId) {
        String deptId = deptUserService.getDeptIdsByUserId(userId);
        //部门角色
        List<String> roleId1 = new ArrayList<>();
        if(deptId != null){
            roleId1 =deptRoleService.getRolsIdsByDeptId(deptId);
        }
        //获取用户单独角色
        List<String> roleId2 = userRoleService.getRoleIdsByUserId(userId);
        //拼接角色
        List<String> roleIds = new ArrayList<String>();;
        roleIds.addAll(roleId1);
        roleIds.addAll(roleId2);
        roleIds = new ArrayList<String>(new LinkedHashSet<>(roleIds));
        if(roleIds.isEmpty()){
            return null;
        }
        List<String> permissionIds= rolePermissionService.getPermissionIdsByRoles(roleIds);
        if (permissionIds.isEmpty()){
            return null;
        }
        List<SysPermission> result=sysPermissionMapper.selectInfoByIds(permissionIds);
        return result;
    }

    /**
     * type=true 递归遍历到菜单
     * type=false 递归遍历到按钮
     * @param all
     * @param type
     * @throws
     */
    private List<MenuRespVO> getTree(List<SysPermission> all, boolean type){

        List<MenuRespVO> list=new ArrayList<>();
        if(all==null||all.isEmpty()){
            return list;
        }
        for(SysPermission sysPermission:all){
            if(sysPermission.getPid().equals("0")){
                MenuRespVO respNodeVO=new MenuRespVO();
//                List<String> roleId = sysRolePermissionMapper.getRoleFromPermission(sysPermission.getId());
                List<String> authorityList = new ArrayList<String>();
                if(!StringUtils.isEmpty(sysPermission.getPerms())){
                    authorityList.add(sysPermission.getPerms());
                }
                respNodeVO.setAuthority(authorityList);
                respNodeVO.setPath(sysPermission.getUrl());
                BeanUtils.copyProperties(sysPermission,respNodeVO);
                if(type){
                    respNodeVO.setRoutes(getChildExBtn(sysPermission.getId(),all));
                }else {
                    respNodeVO.setRoutes(getChild(sysPermission.getId(),all));
                }

                list.add(respNodeVO);
            }
        }
        return list;
    }
    private List<PermissionRespAllVO> getTree2(List<SysPermission> all, boolean type){
        List<PermissionRespAllVO> list=new ArrayList<>();
        if(all==null||all.isEmpty()){
            return list;
        }
        for(SysPermission sysPermission:all){
            if(sysPermission.getPid().equals("0")){
                PermissionRespAllVO respAllVO=new PermissionRespAllVO();
                respAllVO.setKey(sysPermission.getUrl()+sysPermission.getName());
                BeanUtils.copyProperties(sysPermission,respAllVO);
                if(type){
                    respAllVO.setChildren(getChildExBtn2(sysPermission.getId(),all));
                }else {
                    respAllVO.setChildren(getChild2(sysPermission.getId(),all));
                }
                list.add(respAllVO);
            }
        }
        return list;
    }
    private List<MenuTreeRespVO> getTree3(List<SysPermission> all, boolean type){
        List<MenuTreeRespVO> list=new ArrayList<>();
        if(all==null||all.isEmpty()){
            return list;
        }
        for(SysPermission sysPermission:all){
            if(sysPermission.getPid().equals("0")){
                MenuTreeRespVO MenuTreeRespVO=new MenuTreeRespVO();
                BeanUtils.copyProperties(sysPermission,MenuTreeRespVO);
                MenuTreeRespVO.setTitle(sysPermission.getName());
                MenuTreeRespVO.setValue(sysPermission.getId());
                MenuTreeRespVO.setKey(sysPermission.getId());
                if(type){
                    MenuTreeRespVO.setChildren(getChildExBtn3(sysPermission.getId(),all));
                }else {
                }
                list.add(MenuTreeRespVO);
            }
        }
        return list;
    }
    private List<MenuTreeRespVO> getTree4(List<SysPermission> all){
        List<MenuTreeRespVO> list=new ArrayList<>();
        if(all==null||all.isEmpty()){
            return list;
        }
        for(SysPermission sysPermission:all){
            if(sysPermission.getPid().equals("0")){
                MenuTreeRespVO MenuTreeRespVO=new MenuTreeRespVO();
                BeanUtils.copyProperties(sysPermission,MenuTreeRespVO);
                MenuTreeRespVO.setTitle(sysPermission.getName());
                MenuTreeRespVO.setValue(sysPermission.getId());
                MenuTreeRespVO.setKey(sysPermission.getId());
                MenuTreeRespVO.setChildren(getChild3(sysPermission.getId(),all));
                list.add(MenuTreeRespVO);
            }
        }
        return list;
    }
    private List<MenuTreeRespVO> getTree5(List<SysPermission> all){
        List<MenuTreeRespVO> list=new ArrayList<>();
        if(all==null||all.isEmpty()){
            return list;
        }
        for(SysPermission sysPermission:all){
            if(sysPermission.getPid().equals("0")){
                MenuTreeRespVO MenuTreeRespVO=new MenuTreeRespVO();
                BeanUtils.copyProperties(sysPermission,MenuTreeRespVO);
                MenuTreeRespVO.setTitle(sysPermission.getName());
                MenuTreeRespVO.setValue(sysPermission.getId());
                MenuTreeRespVO.setKey(sysPermission.getId());
                MenuTreeRespVO.setChildren(getChild4(sysPermission.getId(),all));
                list.add(MenuTreeRespVO);
            }
        }
        return list;
    }

    /**
     * 递归遍历所有数据
     * @param id
     * @param all
     * @throws
     */
    private List<MenuRespVO> getChild(String id, List<SysPermission> all){

        List<MenuRespVO> list=new ArrayList<>();
        for (SysPermission s:all) {
            if(s.getPid().equals(id)){
                MenuRespVO respNodeVO=new MenuRespVO();
//                List<String> roleId = sysRolePermissionMapper.getRoleFromPermission(s.getId());
                List<String> authorityList = new ArrayList<String>();
                if(!StringUtils.isEmpty(s.getPerms())){
                    authorityList.add(s.getPerms());
                }
                respNodeVO.setAuthority(authorityList);
                respNodeVO.setPath(s.getUrl());
                BeanUtils.copyProperties(s,respNodeVO);
                respNodeVO.setRoutes(getChild(s.getId(),all));
                list.add(respNodeVO);
            }
        }
        return list;
    }
    private List<Object> getChild2(String id,List<SysPermission> all){

        List<Object> list=new ArrayList<>();
        for (SysPermission s: all) {
            if (s.getPid().equals(id) && s.getType()==3){
                PermissionRespBtnVO respBtnVO = new PermissionRespBtnVO();
                BeanUtils.copyProperties(s,respBtnVO);
                respBtnVO.setKey(s.getUrl()+s.getName());
                list.add(respBtnVO);
            }else if(s.getPid().equals(id)){
                PermissionRespAllVO respAllVO=new PermissionRespAllVO();
                BeanUtils.copyProperties(s,respAllVO);
                respAllVO.setKey(s.getUrl());
                respAllVO.setChildren(getChild2(s.getId(),all));
                list.add(respAllVO);
            }
        }
        return list;
    }
    private List<Object> getChild3(String id,List<SysPermission> all){
        List<Object> list=new ArrayList<>();
        for (SysPermission s: all) {
            if (s.getPid().equals(id) && s.getType()==3){
                MenuTreeBtnRespVO menuTreeBtnRespVO = new MenuTreeBtnRespVO();
                menuTreeBtnRespVO.setTitle(s.getName());
                menuTreeBtnRespVO.setValue(s.getId());
                menuTreeBtnRespVO.setKey(s.getId());
                list.add(menuTreeBtnRespVO);
            }else if(s.getPid().equals(id)){
                MenuTreeRespVO menuTreeRespVO=new MenuTreeRespVO();
                menuTreeRespVO.setTitle(s.getName());
                menuTreeRespVO.setValue(s.getId());
                menuTreeRespVO.setKey(s.getId());
                menuTreeRespVO.setChildren(getChild3(s.getId(),all));
                list.add(menuTreeRespVO);
            }
        }
        return list;
    }
    private List<MenuTreeRespVO> getChild4(String id, List<SysPermission> all){
        List<MenuTreeRespVO> list=new ArrayList<MenuTreeRespVO>();
        for (SysPermission s:all) {
            if(s.getPid().equals(id)){
                MenuTreeRespVO menuTreeRespVO=new MenuTreeRespVO();
                menuTreeRespVO.setTitle(s.getName());
                menuTreeRespVO.setValue(s.getId());
                menuTreeRespVO.setKey(s.getId());
                menuTreeRespVO.setChildren(getChild4(s.getId(),all));
                list.add(menuTreeRespVO);
            }
        }
        return list;
    }
    /**
     * 只递归到菜单
     * @param id
     * @param all
     * @throws
     */
    private List<MenuRespVO> getChildExBtn(String id, List<SysPermission> all){
        List<MenuRespVO> list=new ArrayList<>();
        for (SysPermission s:all) {
            if(s.getPid().equals(id)&&s.getType()!=3){
                MenuRespVO respNodeVO=new MenuRespVO();
//                List<String> roleId = sysRolePermissionMapper.getRoleFromPermission(s.getId());
                List<String> authorityList = new ArrayList<String>();
                if(!StringUtils.isEmpty(s.getPerms())){
                    authorityList.add(s.getPerms());
                }
                respNodeVO.setAuthority(authorityList);
                respNodeVO.setPath(s.getUrl());
                BeanUtils.copyProperties(s,respNodeVO);
                respNodeVO.setRoutes(getChildExBtn(s.getId(),all));
                list.add(respNodeVO);
            }
        }
        return list;
    }
    private List<PermissionRespAllVO> getChildExBtn2(String id,List<SysPermission> all){
        List<PermissionRespAllVO> list = new ArrayList<>();
        for (SysPermission s: all) {
            if(s.getPid().equals(id) && s.getType()!=3){
                PermissionRespAllVO respAllVO = new PermissionRespAllVO();
                BeanUtils.copyProperties(s,respAllVO);
                respAllVO.setKey(s.getUrl()+s.getName());
                respAllVO.setChildren(getChildExBtn2(s.getId(),all));
                list.add(respAllVO);
            }
        }
        return list;
    }
    private List<MenuTreeRespVO> getChildExBtn3(String id,List<SysPermission> all){
        List<MenuTreeRespVO> list = new ArrayList<>();
        for (SysPermission s: all) {
            if(s.getPid().equals(id) && s.getType()!=3){
                MenuTreeRespVO MenuTreeRespVO = new MenuTreeRespVO();
                BeanUtils.copyProperties(s,MenuTreeRespVO);
                MenuTreeRespVO.setTitle(s.getName());
                MenuTreeRespVO.setValue(s.getId());
                MenuTreeRespVO.setKey(s.getId());
                MenuTreeRespVO.setChildren(getChildExBtn2(s.getId(),all));
                list.add(MenuTreeRespVO);
            }
        }
        return list;
    }
}
