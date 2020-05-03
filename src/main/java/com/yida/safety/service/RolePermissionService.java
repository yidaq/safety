package com.yida.safety.service;

import com.yida.safety.vo.req.RolePermissionOperationReqVO;

import java.util.List;

/**
 * @program: safety
 * @description: RolePermissionService
 * @author: YiDa
 * @create: 2020-04-24 17:16
 **/
public interface RolePermissionService {

    void addRolePermission(RolePermissionOperationReqVO vo);
    List<String> getPermissionIdsByRoles(List<String> roleIds);
    List<String> getPermissionIdsByRoleId(String roleId);
    int removeRoleByPermissionId(String permissionId);
    List<String> getRoleIdsByPermissionId(String permissionId);
    int removeByRoleId(String roleId);

}
