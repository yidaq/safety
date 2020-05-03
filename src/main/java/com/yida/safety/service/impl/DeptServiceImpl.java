package com.yida.safety.service.impl;

import com.yida.safety.common.BaseResponseCode;
import com.yida.safety.constants.Constant;
import com.yida.safety.dao.*;
import com.yida.safety.exception.BusinessException;
import com.yida.safety.pojo.*;
import com.yida.safety.service.*;
import com.yida.safety.util.CodeUtil;
import com.yida.safety.util.TokenSettings;
import com.yida.safety.vo.req.*;
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
 * @create: 2020-04-28 11:49
 **/
@Service
@Slf4j
public class DeptServiceImpl implements DeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysUserDeptMapper sysUserDeptMapper;
    @Autowired
    private SysDeptRoleMapper sysDeptRoleMapper;
    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private DeptRoleService deptRoleService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private DeptUserService deptUserService;
    @Autowired
    private TokenSettings tokenSettings;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private PermissionService permissionService;

    //查询所有部门
    @Override
    public List<SysDept> selectAll() {
        List<SysDept> list=sysDeptMapper.selectAll();
        for (SysDept s: list) {
            SysDept parent = sysDeptMapper.selectByPrimaryKey(s.getPid());
            if(parent!=null){
                s.setPidName(parent.getName());
            }
        }
        return list;
    }
    //获取部门树
    @Override
    public List<DeptTreeRespVO> deptTreeList(String deptId) {
        List<SysDept> list=sysDeptMapper.selectAll();
        //我要想去掉这个部门的叶子节点，直接在数据源移除这个部门就可以了
        if(!StringUtils.isEmpty(deptId)&&!list.isEmpty()){
            for (SysDept s:list) {
                if(s.getId().equals(deptId)){
                    list.remove(s);
                    break;
                }
            }
        }
        DeptTreeRespVO respNodeVO=new DeptTreeRespVO();
        respNodeVO.setKey("0");
        respNodeVO.setTitle("顶级部门");
        respNodeVO.setValue("0");
        respNodeVO.setChildren(getTree(list));
        List<DeptTreeRespVO> result=new ArrayList<>();
        result.add(respNodeVO);
        return result;
    }
    //获取部门角色
    @Override
    public UserOwnRoleRespVO getDeptOwnRole(String deptId) {
        UserOwnRoleRespVO respVO=new UserOwnRoleRespVO();
        respVO.setOwnRoles(deptRoleService.getRolsIdsByDeptId(deptId));
        List<SysRole> roles = roleService.selectAll();
        List<AllRoleRespVO> allRoleRespVOS = new ArrayList<>();
        for(SysRole sysRole : roles){
            AllRoleRespVO allRoleRespVO = new AllRoleRespVO();
            allRoleRespVO.setTitle(sysRole.getName());
            allRoleRespVO.setKey(sysRole.getId());
            if(sysRole.getStatus() == 0 ) {
                allRoleRespVO.setDisabled(true);
            }else {
                allRoleRespVO.setDisabled(false);
            }
            allRoleRespVOS.add(allRoleRespVO);
        }
        respVO.setAllRole(allRoleRespVOS);
        return respVO;
    }
    //修改部门角色
    @Override
    public void setDeptOwnRole(DeptOwnRoleReqVO vo) {
        deptRoleService.addDeptRoleInfo(vo);
        List<String> users =  deptUserService.getUserIdByDeptId(vo.getDeptId());
        if(users.size() != 0){
            for(int i = 0 ; i<users.size();i++){
                /**
                 * 标记用户 要主动去刷新
                 */
                redisService.set(Constant.JWT_REFRESH_KEY+users.get(i),users.get(i),tokenSettings.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                /**
                 * 清楚用户授权数据缓存
                 */
                redisService.delete(Constant.IDENTIFY_CACHE_KEY+users.get(i));
            }
        }

    }
    //新增部门
    @Override
    public SysDept addDept(DeptAddReqVO vo) {
        String relationCode;
        long deptCount=redisService.incrby(Constant.DEPT_CODE_KEY,1);
        String deptCode= CodeUtil.deptCode(String.valueOf(deptCount),7,"0");
        SysDept parent=sysDeptMapper.selectByPrimaryKey(vo.getPid());
        if(vo.getPid().equals("0")){
            relationCode=deptCode;
        }else if(null==parent){
            log.info("父级数据不存在{}",vo.getPid());
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }else {
            relationCode=parent.getRelationCode()+deptCode;
        }
        SysDept sysDept=new SysDept();
        BeanUtils.copyProperties(vo,sysDept);
        sysDept.setDeptManagerId(vo.getManagerId());
        sysDept.setManagerName(sysUserMapper.selectByPrimaryKey(vo.getManagerId()).getNickName());
        sysDept.setId(UUID.randomUUID().toString());
        sysDept.setCreateTime(new Date());
        sysDept.setDeptNo(deptCode);
        sysDept.setAvatar("https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=2110203088,3109516441&fm=26&gp=0.jpg");
        sysDept.setRelationCode(relationCode);
        sysUserMapper.updateDeptId(vo.getManagerId(),sysDept.getId());
        int i = sysDeptMapper.insertSelective(sysDept);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        SysUserDept sysUserDept = new SysUserDept();
        sysUserDept.setId(UUID.randomUUID().toString());
        sysUserDept.setDeptId(sysDept.getId());
        sysUserDept.setUserId(sysDept.getDeptManagerId());
        sysUserDept.setCreateTime(new Date());
        int count = sysUserDeptMapper.insertSelective(sysUserDept);
        if(count != 1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        return sysDept;
    }

    //获取部门成员
    @Override
    public List<SysUser> getUsersByDeptId(String deptId) {
        List<String> userId = deptUserService.getUserIdByDeptId(deptId);
        List<SysUser> users = new ArrayList<SysUser>();
        if(userId.size() != 0) {
            users = sysUserMapper.getDeptUsers(userId);
        }
        return users;
    }
    //根据id获取部门所有权限
    @Override
    public List<SysPermission> getDeptPermission(String deptId) {
        List<String> roleId = deptRoleService.getRolsIdsByDeptId(deptId);
        List<String> permissions = new ArrayList<>();
        if (roleId.size() != 0){
           permissions = rolePermissionService.getPermissionIdsByRoles(roleId);
        }
        permissions = new ArrayList<>(new LinkedHashSet<>(permissions));
        return permissionService.getPermissionsByIds(permissions);
    }
    //根据id获取部门信息
    @Override
    public SysDept getDeptInfoById(String deptid) {
        return sysDeptMapper.selectByPrimaryKey(deptid);
    }
    //获取没有部门的用户
    @Override
    public List<SelectUserRespVO> getExDeptUser() {
        List<SelectUserRespVO> list = new ArrayList<>();
        List<String> userId = new ArrayList<>();
        List<SysUserDept> sysUserDepts = deptUserService.selectAll();
        for(SysUserDept item : sysUserDepts){
            userId.add(item.getUserId());
        }
        if(userId.size() == 0){
            List<SysUser> all = sysUserMapper.getAll();
            if(all.size() != 0){
                for(SysUser user : all ){
                    SelectUserRespVO userRespVO = new SelectUserRespVO();
                    userRespVO.setTitle(user.getNickName());
                    userRespVO.setValue(user.getId());
                    list.add(userRespVO);
                }
            }
            return list;
        }
        List<SysUser> userList =sysUserMapper.getExtIds(userId);
        if(userList.size() != 0){
            for(SysUser user : userList ){
                SelectUserRespVO userRespVO = new SelectUserRespVO();
                userRespVO.setTitle(user.getNickName());
                userRespVO.setValue(user.getId());
                list.add(userRespVO);
            }
        }
        return list;
    }
    //添加对应部门用户
    @Override
    public void setDeptUsers(AddDeptUsersReqVO vo) {
        for(String userid : vo.getUserId()){
            SysUserDept sysUserDept = new SysUserDept();
            sysUserDept.setId(UUID.randomUUID().toString());
            sysUserDept.setUserId(userid);
            sysUserDept.setCreateTime(new Date());
            sysUserDept.setDeptId(vo.getDeptId());
            sysUserDeptMapper.insertSelective(sysUserDept);
            //添加用户表部门
            SysUser sysUser = sysUserMapper.selectByPrimaryKey(userid);
            sysUser.setDeptId(vo.getDeptId());
            sysUserMapper.updateByPrimaryKeySelective(sysUser);
        }
    }
    //删除部门用户
    @Override
    public void deleteDeptUser(DeleteDeptUser deleteDeptUser) {
        int i = sysUserDeptMapper.deleteKeyByDeptUserId(deleteDeptUser);
        int result = sysUserMapper.updateDeptId(deleteDeptUser.getUserId(),null);
        if(result!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        if( i != 1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        /**
         * 标记用户 要主动去刷新
         */
        redisService.set(Constant.JWT_REFRESH_KEY+deleteDeptUser.getUserId(),deleteDeptUser.getUserId(),tokenSettings.getAccessTokenExpireTime().toMillis(),TimeUnit.MILLISECONDS);
        /**
         * 清楚用户授权数据缓存
         */
        redisService.delete(Constant.IDENTIFY_CACHE_KEY+deleteDeptUser.getUserId());
    }
    //删除部门接口
    @Override
    public void deletedDept(String id) {
        //查找它和它的叶子节点
        SysDept sysDept=sysDeptMapper.selectByPrimaryKey(id);
        if(sysDept==null){
            log.info("传入的部门id在数据库不存在{}",id);
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        List<String> list = sysDeptMapper.selectChildIds(sysDept.getRelationCode());

        //判断它和它子集的叶子节点是否关联有用户
        List<String> sysUsers = sysUserDeptMapper.selectUserInfoByDeptIds(list);
        if(!sysUsers.isEmpty()){
            throw new BusinessException(BaseResponseCode.NOT_PERMISSION_DELETED_DEPT);
        }
        //逻辑删除部门数据
        int count=sysDeptMapper.deletedDepts(new Date(),list);
        if(count==0){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        String aaa = sysDeptRoleMapper.selectBydeptId(id);
        if (aaa!=null && !aaa.isEmpty()){
            //删除部门角色信息
            int i = sysDeptRoleMapper.removeRoleByDeptId(id);
            if (i == 0){
                throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
            }
        }
    }
    //获取部门资源
    @Override
    public DeptOwnPermissionRespVO getDeptPermissions(String deptId,String userId) {
        //获取部门角色
       List<String> roleIds = deptRoleService.getRolsIdsByDeptId(deptId);
        //获取部门权限集合
        List<String> permissions = new ArrayList<>();
       if(roleIds.size() != 0){
           permissions = rolePermissionService.getPermissionIdsByRoles(roleIds);
       }
        List<AllRoleRespVO> allRoleRespVOS = new ArrayList<>();
        if (permissions.size() != 0){
            for (String item:permissions){
                AllRoleRespVO allRoleRespVO = new AllRoleRespVO();
                SysPermission sysPermission = sysPermissionMapper.selectByPrimaryKey(item);
                allRoleRespVO.setKey(sysPermission.getId());
                allRoleRespVO.setTitle(sysPermission.getName());
                allRoleRespVO.setDisabled(false);
                allRoleRespVOS.add(allRoleRespVO);
            }
        }
       //获取部门成员权限
       List<String> userPermissions = deptUserService.getRoleIdsByUserId(userId);
        if(StringUtils.isEmpty(userPermissions.get(0))){
            userPermissions = new ArrayList<>();
        }
       DeptOwnPermissionRespVO deptOwnPermissionRespVO = new DeptOwnPermissionRespVO();
       deptOwnPermissionRespVO.setAllPermissions(allRoleRespVOS);
       deptOwnPermissionRespVO.setOwnPermissions(userPermissions);
        return deptOwnPermissionRespVO;
    }

    @Override
    public void updateUserPermission(DeptUpdateUserPeEeqVO vo) {
        
    }


    private List<DeptTreeRespVO> getTree(List<SysDept> all){
        List<DeptTreeRespVO> list=new ArrayList<>();
        for (SysDept s:all) {
            if(s.getPid().equals("0")){
                DeptTreeRespVO respNodeVO=new DeptTreeRespVO();
                respNodeVO.setKey(s.getId());
                respNodeVO.setTitle(s.getName());
                respNodeVO.setValue(s.getId());
                respNodeVO.setChildren(getChild(s.getId(),all));
                list.add(respNodeVO);
            }
        }
        return list;
    }

    private List<DeptTreeRespVO> getChild(String id, List<SysDept> all){
        List<DeptTreeRespVO> list=new ArrayList<>();
        for (SysDept s :all) {
            if(s.getPid().equals(id)){
                DeptTreeRespVO deptRespNodeVO=new DeptTreeRespVO();
                deptRespNodeVO.setKey(s.getId());
                deptRespNodeVO.setTitle(s.getName());
                deptRespNodeVO.setValue(s.getId());
                deptRespNodeVO.setChildren(getChild(s.getId(),all));
                list.add(deptRespNodeVO);
            }
        }
        return list;
    }
}
