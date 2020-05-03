package com.yida.safety.service.impl;

import com.github.pagehelper.PageHelper;
import com.yida.safety.common.BaseResponseCode;
import com.yida.safety.constants.Constant;
import com.yida.safety.dao.SysRoleMapper;
import com.yida.safety.exception.BusinessException;
import com.yida.safety.pojo.SysRole;
import com.yida.safety.service.*;
import com.yida.safety.util.PageUtil;
import com.yida.safety.util.TokenSettings;
import com.yida.safety.vo.PageVO;
import com.yida.safety.vo.req.AddRoleReqVO;
import com.yida.safety.vo.req.RolePageReqVO;
import com.yida.safety.vo.req.RolePermissionOperationReqVO;
import com.yida.safety.vo.req.RoleUpdateReqVO;
import com.yida.safety.vo.resp.MenuTreeRespVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @program: safety
 * @description: RoleServiceImpl
 * @author: YiDa
 * @create: 2020-04-25 16:31
 **/
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TokenSettings tokenSettings;
    @Autowired
    private DeptUserService deptUserService;
    @Autowired
    private DeptRoleService deptRoleService;

    @Override
    public PageVO<SysRole> pageInfo(RolePageReqVO vo) {
        PageHelper.startPage(vo.getPageNum(),vo.getPageSize());
        List<SysRole> sysRoles =sysRoleMapper.selectAll(vo);
        return PageUtil.getPageVO(sysRoles);
    }
    //添加角色
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole addRole(AddRoleReqVO vo) {
        SysRole sysRole=new SysRole();
        BeanUtils.copyProperties(vo,sysRole);
        sysRole.setId(UUID.randomUUID().toString());
        sysRole.setCreateTime(new Date());
        int i = sysRoleMapper.insertSelective(sysRole);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        if(vo.getPermissions()!=null&&!vo.getPermissions().isEmpty()){
            RolePermissionOperationReqVO operationReqVO=new RolePermissionOperationReqVO();
            operationReqVO.setRoleId(sysRole.getId());
            operationReqVO.setPermissionIds(vo.getPermissions());
            rolePermissionService.addRolePermission(operationReqVO);
        }
        return sysRole;
    }
    //获取所有角色
    @Override
    public List<SysRole> selectAll() {
        return sysRoleMapper.selectAll(new RolePageReqVO());
    }
    //获取用户所拥有的角色id
    @Override
    public List<String> getRoleNames(String userId) {

        List<SysRole> sysRoles=getRoleInfoByUserId(userId);
        if (null==sysRoles||sysRoles.isEmpty()){
            return null;
        }
        List<String> list=new ArrayList<>();
        for (SysRole sysRole:sysRoles){
            list.add(sysRole.getName());
        }
        return list;
    }
    //获取用户所拥有的角色信息
    @Override
    public List<SysRole> getRoleInfoByUserId(String userId) {

        List<String> roleIds=userRoleService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()){
            return null;
        }
        return sysRoleMapper.getRoleInfoByIds(roleIds);
    }
    //删除角色
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletedRole(String roleId) {
        //就更新删除的角色数据
        SysRole sysRole=new SysRole();
        sysRole.setId(roleId);
        sysRole.setDeleted(0);
        sysRole.setUpdateTime(new Date());
        int i = sysRoleMapper.updateByPrimaryKeySelective(sysRole);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //角色菜单权限关联数据删除
        rolePermissionService.removeByRoleId(roleId);
        List<String> userIdsBtRoleId = userRoleService.getUserIdsBtRoleId(roleId);
        List<String> deptIds = deptRoleService.getDeptIdsByRoleId(roleId);
        List<String> userIdsByDeptIds = new ArrayList<>();
        if(deptIds.size() != 0){
            //角色部门关联数据删除
            deptRoleService.removeKeyByRoleId(roleId);
            for(String id : deptIds){
                List<String> userId  = deptUserService.getUserIdByDeptId(id);
                userIdsByDeptIds.addAll(userId);
            }
        }
        List<String> userIds = new ArrayList<>();
        userIds.addAll(userIdsBtRoleId);
        userIds.addAll(userIdsByDeptIds);
        userIds = new ArrayList<>(new LinkedHashSet<>(userIds));
        //角色用户关联数据删除
        userRoleService.removeUserRoleId(roleId);
        //把跟该角色关联的用户标记起来，需要刷新token
        if(!userIds.isEmpty()){
            for (String userId:userIds) {
                /**
                 * 标记用户 在用户认证的时候判断这个是否主动刷过
                 */
                redisService.set(Constant.JWT_REFRESH_KEY+userId,userId,tokenSettings.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                /**
                 * 清楚用户授权数据缓存
                 */
                redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
            }
        }
    }

    @Override
    public SysRole detailInfo(String id) {
        //通过id获取角色信息
        SysRole sysRole = sysRoleMapper.selectByPrimaryKey(id);
        if(sysRole==null){
            log.error("传入 的 id:{}不合法",id);
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        //获取所有权限菜单权限树
        List<MenuTreeRespVO> menuTreeRespVOS = permissionService.selectAllTree();
        //获取该角色拥有的菜单权限
        List<String> permissionIdsByRoleId = rolePermissionService.getPermissionIdsByRoleId(id);
        //遍历菜单权限树的数据
        sysRole.setPermissions(permissionIdsByRoleId);
        sysRole.setMenuTreeRespVO(menuTreeRespVOS);
        return sysRole;
    }
    //修改角色
    @Override
    public void updateRole(RoleUpdateReqVO vo) {
        //保存角色基本信息
        SysRole sysRole=sysRoleMapper.selectByPrimaryKey(vo.getId());
        if (null==sysRole){
            log.error("传入 的 id:{}不合法",vo.getId());
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        BeanUtils.copyProperties(vo,sysRole);
        sysRole.setUpdateTime(new Date());
        int count=sysRoleMapper.updateByPrimaryKeySelective(sysRole);
        if(count!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //修改该角色和菜单权限关联数据
        RolePermissionOperationReqVO reqVO=new RolePermissionOperationReqVO();
        reqVO.setRoleId(vo.getId());
        reqVO.setPermissionIds(vo.getPermissions());
        rolePermissionService.addRolePermission(reqVO);
        //标记关联用户
        List<String> userIdsBtRoleId = userRoleService.getUserIdsBtRoleId(vo.getId());
        List<String> deptIds = deptRoleService.getDeptIdsByRoleId(vo.getId());
        List<String> userIdsByDeptIds = new ArrayList<>();
        if(deptIds.size() != 0){
            for(String id : deptIds){
                List<String> userId  = deptUserService.getUserIdByDeptId(id);
                userIdsByDeptIds.addAll(userId);
            }
        }
        List<String> userIds = new ArrayList<>();
        userIds.addAll(userIdsBtRoleId);
        userIds.addAll(userIdsByDeptIds);
        userIds = new ArrayList<>(new LinkedHashSet<>(userIds));
        if(!userIdsBtRoleId.isEmpty()){
            for (String userId:userIdsBtRoleId) {
                /**
                 * 标记用户 在用户认证的时候判断这个是否主动刷过
                 */
                redisService.set(Constant.JWT_REFRESH_KEY+userId,userId,tokenSettings.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                /**
                 * 清楚用户授权数据缓存
                 */
                redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
            }
        }
    }


}
