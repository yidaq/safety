package com.yida.safety.service;

import com.yida.safety.pojo.SysUser;
import com.yida.safety.vo.PageVO;
import com.yida.safety.vo.req.*;
import com.yida.safety.vo.resp.LoginRespVO;
import com.yida.safety.vo.resp.SelectUserRespVO;
import com.yida.safety.vo.resp.UserInfoRespVO;
import com.yida.safety.vo.resp.UserOwnRoleRespVO;

import java.util.List;

/**
 * @program: safety
 * @description: userService
 * @author: YiDa
 * @create: 2020-04-24 11:31
 **/
public interface UserService {

    LoginRespVO login(LoginReqVO user);
    void logout(String accessToken, String refreshToken);
    UserInfoRespVO getCurrentUser(String userId);
    String refreshToken(String refreshToken);
    PageVO<SysUser> pageInfo(UserPageReqVO vo);
    void addUser(UserAddReqVO vo);
    UserOwnRoleRespVO getUserOwnRole(String userId);
    void setUserOwnRole(UserOwnRoleReqVO vo);
    void deletedUsers(List<String> list, String operationId);
    void updateUserInfo(UserUpdateReqVO vo, String operationId);
    UserInfoRespVO userUpdateDetailInfo(UserUpdateDetailInfoReqVO vo, String userId);
    void userUpdatePwd(UserUpdatePwdReqVO vo, String accessToken, String refreshToken);
    List<SysUser> getLoginUser();
    List<SelectUserRespVO> getUserByKey(String key);
    List<String> getUserOwnPermisson(String userId);
}
