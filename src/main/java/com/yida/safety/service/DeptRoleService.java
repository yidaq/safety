package com.yida.safety.service;

import com.yida.safety.vo.req.DeptOwnRoleReqVO;


import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-29 11:35
 **/
public interface DeptRoleService {

    List<String> getRolsIdsByDeptId(String deptId);

    void addDeptRoleInfo(DeptOwnRoleReqVO vo);

    List<String> getDeptIdsByRoleId(String roleId);

    void removeKeyByRoleId(String roleId);

}
