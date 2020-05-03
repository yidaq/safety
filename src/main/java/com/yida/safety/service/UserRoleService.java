package com.yida.safety.service;

import com.yida.safety.vo.req.UserOwnRoleReqVO;

import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-25 16:33
 **/
public interface UserRoleService {

    List<String> getRoleIdsByUserId(String userId);
    List<String> getUserIdsByRoleIds(List<String> roleIds);
    List<String> getUserIdsBtRoleId(String roleId);
    int removeUserRoleId(String roleId);
    void addUserRoleInfo(UserOwnRoleReqVO vo);

}
