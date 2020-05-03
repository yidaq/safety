package com.yida.safety.service.impl;

import com.github.pagehelper.PageHelper;
import com.yida.safety.common.BaseResponseCode;
import com.yida.safety.constants.Constant;
import com.yida.safety.dao.SysDeptMapper;
import com.yida.safety.dao.SysUserDeptMapper;
import com.yida.safety.dao.SysUserMapper;
import com.yida.safety.exception.BusinessException;
import com.yida.safety.pojo.SysDept;
import com.yida.safety.pojo.SysRole;
import com.yida.safety.pojo.SysUser;
import com.yida.safety.pojo.SysUserDept;
import com.yida.safety.service.*;
import com.yida.safety.util.JwtTokenUtil;
import com.yida.safety.util.PageUtil;
import com.yida.safety.util.PasswordUtils;
import com.yida.safety.util.TokenSettings;
import com.yida.safety.vo.PageVO;
import com.yida.safety.vo.req.*;
import com.yida.safety.vo.resp.*;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
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
 * @create: 2020-04-24 11:31
 **/
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysDeptMapper deptMapper;
    @Autowired
    private SysUserDeptMapper sysUserDeptMapper;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private TokenSettings tokenSettings;
    @Autowired
    private DeptUserService deptUserService;

    @Override
    public LoginRespVO login(LoginReqVO user) {
        SysUser loginUser = sysUserMapper.getUserInfoByName(user.getUsername());
        //判断用户是否存在
        if(loginUser==null){
            throw new BusinessException(BaseResponseCode.ACCOUNT_ERROR);
        }
        //判断用户是否锁定
        if(loginUser.getStatus() == 2){
            throw new BusinessException(BaseResponseCode.ACCOUNT_LOCK_TIP);
        }
        //判断用户密码是否正确
        if(!PasswordUtils.matches(loginUser.getSalt(),user.getPassword(),loginUser.getPassword())){
            throw new BusinessException(BaseResponseCode.ACCOUNT_PASSWORD_ERROR);
        }
        //创建返回vo
        LoginRespVO loginRespVO=new LoginRespVO();
        loginRespVO.setPhone(loginUser.getPhone());
        loginRespVO.setUsername(loginUser.getUsername());
        loginRespVO.setId(loginUser.getId());

        List<String> permissionList = getPermissionsByUserId(loginUser.getId());
        List<String> roleList = getRolesByUserId(loginUser.getId());

        List<String> currentAuthority = new ArrayList<String>();
        if(permissionList != null && !permissionList.isEmpty()){
            currentAuthority.addAll(permissionList);
        }
        if(roleList != null && !roleList.isEmpty()){
            currentAuthority.addAll(roleList);
        }
//        System.out.println(currentAuthority);
        loginRespVO.setCurrentAuthority(currentAuthority);

        Map<String, Object> claims=new HashMap<>();
        //获取用户角色
        claims.put(Constant.JWT_ROLES_KEY,getRolesByUserId(loginUser.getId()));
        //获取用户权限
        claims.put(Constant.JWT_PERMISSIONS_KEY,getPermissionsByUserId(loginUser.getId()));
        //获取用户名
        claims.put(Constant.JWT_USER_NAME,loginUser.getUsername());
        //获取jwt
        String accessToken = JwtTokenUtil.getAccessToken(loginUser.getId(),claims);
        String refreshToken = null;
        //判断登陆端类型
        if(user.getType().equals("1")){
            //获取刷新jwt
            refreshToken=JwtTokenUtil.getRefreshToken(loginUser.getId(),claims);
        }else {
            refreshToken=JwtTokenUtil.getRefreshAppToken(loginUser.getId(),claims);
        }
        loginRespVO.setAccessToken(accessToken);
        loginRespVO.setRefreshToken(refreshToken);
        //在缓存中加入用户
        redisService.set(loginUser.getId()+"loginUser",accessToken,JwtTokenUtil.getRemainingTime(accessToken),TimeUnit.MILLISECONDS);
        return loginRespVO;
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if(StringUtils.isEmpty(accessToken)||StringUtils.isEmpty(refreshToken)){
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        Subject subject = SecurityUtils.getSubject();
        log.info("subject.getPrincipals()={}",subject.getPrincipals());
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        String userId=JwtTokenUtil.getUserId(accessToken);
        /**
         * 把token 加入黑名单 禁止再登录
         **/
        redisService.set(Constant.JWT_ACCESS_TOKEN_BLACKLIST+accessToken,userId,JwtTokenUtil.getRemainingTime(accessToken), TimeUnit.MILLISECONDS);
        /**
         * 把 refreshToken 加入黑名单 禁止再拿来刷新token
         **/
        redisService.set(Constant.JWT_REFRESH_TOKEN_BLACKLIST+refreshToken,userId,JwtTokenUtil.getRemainingTime(refreshToken),TimeUnit.MILLISECONDS);
        //在缓存中删除用户
        redisService.delete(JwtTokenUtil.getUserId(accessToken)+"loginUser");
    }
    /**
     * @Description:  获取用户个人信息
     */
    @Override
    public UserInfoRespVO getCurrentUser(String userId) {
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        UserInfoRespVO userInfoRespVO = new UserInfoRespVO();
        if (sysUser != null){
            BeanUtils.copyProperties(sysUser,userInfoRespVO);
            userInfoRespVO.setName(sysUser.getNickName());
            if(sysUser.getDeptId() !=null && !sysUser.getDeptId().isEmpty()){
                userInfoRespVO.setGroup(deptMapper.selectByPrimaryKey(sysUser.getDeptId()).getName());
            }else {
                userInfoRespVO.setGroup("还没有部门哦");
            }
            userInfoRespVO.setRole(getRolesByUserId(userId));
        }
        return userInfoRespVO;
    }
    /**
     * @Description:  刷新token
     */
    @Override
    public String refreshToken(String refreshToken) {
        //它是否过期
        //它是否被加入了黑名
        if(!JwtTokenUtil.validateToken(refreshToken)||redisService.hasKey(Constant.JWT_REFRESH_TOKEN_BLACKLIST+refreshToken)){
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }
        String userId=JwtTokenUtil.getUserId(refreshToken);
        log.info("userId={}",userId);
        Map<String,Object> claims=null;
        if(redisService.hasKey(Constant.JWT_REFRESH_KEY+userId)){
            claims=new HashMap<>();
            claims.put(Constant.JWT_ROLES_KEY,getRolesByUserId(userId));
            claims.put(Constant.JWT_PERMISSIONS_KEY,getPermissionsByUserId(userId));
        }
        String newAccessToken=JwtTokenUtil.refreshToken(refreshToken,claims);
        //刷新之后重新插入
        redisService.delete(userId+"loginUser");
        redisService.set(userId+"loginUser",newAccessToken,JwtTokenUtil.getRemainingTime(newAccessToken),TimeUnit.MILLISECONDS);
        return newAccessToken;
    }
    /**
     * @Description:  分页查询用户
     */
    @Override
    public PageVO<SysUser> pageInfo(UserPageReqVO vo) {
        PageHelper.startPage(vo.getPageNum(),vo.getPageSize());
        List<SysUser> list=sysUserMapper.selectAll(vo);
        for (SysUser sysUser:list){
            SysDept sysDept = sysDeptMapper.selectByPrimaryKey(sysUser.getDeptId());
            if(sysDept!=null){
                sysUser.setDeptName(sysDept.getName());
            }
        }
        return PageUtil.getPageVO(list);
    }
    /**
     * @Description:  添加用户
     */
    @Override
    public void addUser(UserAddReqVO vo) {
        SysUser sysUser=new SysUser();
        if(vo.getDeptId().equals("0")) {
            vo.setDeptId(null);
        }
        BeanUtils.copyProperties(vo,sysUser);
        sysUser.setId(UUID.randomUUID().toString());
        sysUser.setCreateTime(new Date());
        String salt=PasswordUtils.getSalt();
        String ecdPwd=PasswordUtils.encode(vo.getPassword(),salt);
        sysUser.setSalt(salt);
        sysUser.setPassword(ecdPwd);
        int i = sysUserMapper.insertSelective(sysUser);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }
    /**
    * @Description:  获取用户角色
    */
    @Override
    public UserOwnRoleRespVO getUserOwnRole(String userId) {
        UserOwnRoleRespVO respVO=new UserOwnRoleRespVO();
//        String deptId = userDeptSerivce.getDeptIdByUserId(userId);
//        List<String> roleIds = deptRoleService.getRolsIdsByDeptId(deptId);
        respVO.setOwnRoles(userRoleService.getRoleIdsByUserId(userId));
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
    /**
    * @Description:  添加用户角色
    */
    @Override
    public void setUserOwnRole(UserOwnRoleReqVO vo) {
        userRoleService.addUserRoleInfo(vo);
        /**
         * 标记用户 要主动去刷新
         */
        redisService.set(Constant.JWT_REFRESH_KEY+vo.getUserId(),vo.getUserId(),tokenSettings.getAccessTokenExpireTime().toMillis(),TimeUnit.MILLISECONDS);
        /**
         * 清楚用户授权数据缓存
         */
        redisService.delete(Constant.IDENTIFY_CACHE_KEY+vo.getUserId());
    }
    /**
    * @Description: 批量删除用户
    */
    @Override
    public void deletedUsers(List<String> list, String operationId) {
        SysUser sysUser=new SysUser();
        sysUser.setUpdateId(operationId);
        sysUser.setUpdateTime(new Date());
        int i = sysUserMapper.deletedUsers(sysUser, list);
        if(i==0){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        for (String userId: list) {
            redisService.set(Constant.DELETED_USER_KEY+userId,userId,tokenSettings.getRefreshTokenExpireAppTime().toMillis(),TimeUnit.MILLISECONDS);
            /**
             * 清楚用户授权数据缓存
             */
            redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
        }
    }
    /**
    * @Description: 用户表单修改用户
    */
    @Override
    public void updateUserInfo(UserUpdateReqVO vo, String operationId) {
        //判断部门更改
        SysUser dbUser = sysUserMapper.selectByPrimaryKey(vo.getId());
        if(vo.getDeptId().equals("0")) {
            vo.setDeptId(null);
        }
        if(dbUser.getDeptId() != vo.getDeptId()){
            deptUserService.deleteKeyByUserId(vo.getId());
            SysUserDept sysUserDept = new SysUserDept();
            sysUserDept.setId(UUID.randomUUID().toString());
            sysUserDept.setUserId(vo.getId());
            sysUserDept.setDeptId(vo.getDeptId());
            sysUserDeptMapper.insertSelective(sysUserDept);
            /**
             * 标记用户 要主动去刷新
             */
            redisService.set(Constant.JWT_REFRESH_KEY+vo.getId(),vo.getId(),tokenSettings.getAccessTokenExpireTime().toMillis(),TimeUnit.MILLISECONDS);
            /**
             * 清楚用户授权数据缓存
             */
            redisService.delete(Constant.IDENTIFY_CACHE_KEY+vo.getId());
        }
        //修改用户
        SysUser sysUser=new SysUser();
        BeanUtils.copyProperties(vo,sysUser);
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(operationId);
        if (StringUtils.isEmpty(sysUser.getAvatar())){
            sysUser.setAvatar("https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png");
        }
        if(StringUtils.isEmpty(vo.getPassword())){
            sysUser.setPassword(null);
        }else {
            String salt=PasswordUtils.getSalt();
            String endPwd=PasswordUtils.encode(vo.getPassword(),salt);
            sysUser.setSalt(salt);
            sysUser.setPassword(endPwd);
        }
        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        if(vo.getStatus()==2){
            redisService.set(Constant.ACCOUNT_LOCK_KEY+vo.getId(),vo.getId());
        }else {
            redisService.delete(Constant.ACCOUNT_LOCK_KEY+vo.getId());
        }
    }
    /**
     * @Description:  修改用户个人信息
     */
    @Override
    public UserInfoRespVO userUpdateDetailInfo(UserUpdateDetailInfoReqVO vo, String userId) {
        SysUser sysUser=new SysUser();
        BeanUtils.copyProperties(vo,sysUser);
        sysUser.setId(userId);
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(userId);
        sysUser.setNickName(vo.getNickName());
        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        return getCurrentUser(userId);
    }
    /**
     * @Description:  修改用户个人密码
     */
    @Override
    public void userUpdatePwd(UserUpdatePwdReqVO vo, String accessToken, String refreshToken) {
        String userId=JwtTokenUtil.getUserId(accessToken);
        //校验旧密码
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        if(sysUser==null){
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }
        if(!PasswordUtils.matches(sysUser.getSalt(),vo.getOldPwd(),sysUser.getPassword())){
            throw new BusinessException(BaseResponseCode.OLD_PASSWORD_ERROR);
        }
        //保存新密码
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(userId);
        sysUser.setPassword(PasswordUtils.encode(vo.getNewPwd(),sysUser.getSalt()));
        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        /**
         * 把token 加入黑名单 禁止再访问我们的系统资源
         */
        redisService.set(Constant.JWT_ACCESS_TOKEN_BLACKLIST+accessToken,userId,JwtTokenUtil.getRemainingTime(accessToken), TimeUnit.MILLISECONDS);
        /**
         * 把 refreshToken 加入黑名单 禁止再拿来刷新token
         */
        redisService.set(Constant.JWT_REFRESH_TOKEN_BLACKLIST+refreshToken,userId,JwtTokenUtil.getRemainingTime(refreshToken),TimeUnit.MILLISECONDS);

        /**
         * 清楚用户授权数据缓存
         */
        redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
    }

    /**
    * @Description:  获取在线用户列表
    */
    @Override
    public List<SysUser> getLoginUser() {
        List<String> list =new ArrayList(redisService.keys("*loginUser")) ;
        List<SysUser> userList = new ArrayList<SysUser>();
        for (int i = 0 ; i < list.size(); i++){
            String userid = list.get(i).substring(0,list.get(i).indexOf("loginUser"));
            userList.add(sysUserMapper.selectByPrimaryKey(userid));
        }
        return userList;
    }
    /**
    * @Description:  模糊查询
    */
    @Override
    public List<SelectUserRespVO> getUserByKey(String key) {
        List<SysUser> sysUsers =  sysUserMapper.getUserByKey(key);
        List<SelectUserRespVO> selectUserRespVOS = new ArrayList<>();
        for(SysUser sysUser:sysUsers){
            SelectUserRespVO selectUserRespVO = new SelectUserRespVO();
            selectUserRespVO.setTitle(sysUser.getNickName());
            selectUserRespVO.setValue(sysUser.getId());
            selectUserRespVOS.add(selectUserRespVO);
        }
        return selectUserRespVOS;
    }

    @Override
    public List<String> getUserOwnPermisson(String userId) {
        return getPermissionsByUserId(userId);
    }


    /**
     * @Description: 获取用户角色
     * @Param: [userId]
     * @return: java.util.List<java.lang.String>
     * @Author: YiDa
     * @Date: 2020/3/18
     */
    private List<String> getRolesByUserId(String userId){
        return  roleService.getRoleNames(userId);
    }

    /**
     * @Description: 获取用户权限
     * @Param: [userId]
     * @return: java.util.List<java.lang.String>
     * @Author: YiDa
     * @Date: 2020/3/18
     */
    private List<String> getPermissionsByUserId(String userId){
        return  permissionService.getPermissionsByUserId(userId);
    }


}
