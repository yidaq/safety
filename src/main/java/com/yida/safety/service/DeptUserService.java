package com.yida.safety.service;

import com.yida.safety.pojo.SysUserDept;

import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-29 11:45
 **/
public interface DeptUserService {

    String getDeptIdsByUserId(String userId);
    List<String> getUserIdByDeptId(String deptId);
    List<String> getRoleIdsByUserId(String userId);
    List<SysUserDept> selectAll();
    void deleteKeyByUserId(String userId);
}
