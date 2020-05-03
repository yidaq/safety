package com.yida.safety.service;

import com.yida.safety.pojo.SysPermission;
import com.yida.safety.vo.req.PermissionAddReqVO;
import com.yida.safety.vo.req.PermissionUpdateReqVO;
import com.yida.safety.vo.resp.MenuRespVO;
import com.yida.safety.vo.resp.MenuTreeRespVO;
import com.yida.safety.vo.resp.PermissionRespAllVO;

import java.util.List;

/**
 * @program: safety
 * @description: permissionService
 * @author: YiDa
 * @create: 2020-04-24 17:14
 **/
public interface PermissionService {

    List<SysPermission> selectAll();
    List<MenuTreeRespVO> selectAllTree();
    List<MenuRespVO> getMenu(String userId);
    List<MenuRespVO> getPermission();
    List<PermissionRespAllVO> selectPermissionTable();
    List<String> getPermissionsByUserId(String userId);
    SysPermission addPermission(PermissionAddReqVO vo);
    List<MenuTreeRespVO> selectMenuTree(String type);
    void deletedPermission(String permissionId);
    void updatePermission(PermissionUpdateReqVO vo);

    List<SysPermission> getPermissionsByIds(List<String> permissionIds);


}
