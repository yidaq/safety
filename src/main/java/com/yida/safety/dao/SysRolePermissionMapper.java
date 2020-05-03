package com.yida.safety.dao;

import com.yida.safety.pojo.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRolePermissionMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysRolePermission record);

    int insertSelective(SysRolePermission record);

    SysRolePermission selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysRolePermission record);

    int updateByPrimaryKey(SysRolePermission record);

    List<String> getRoleFromPermission(String permissionId);

    List<String> getPermissionIdsByRoles(List<String> roleIds);

    //根据permissionId 删除角色和菜单权限关联表相关数据
    int removeByPermissionId(String permissionId);

    List<String> getRoleIdsByPermissionId(String permissionId);

    //根据角色id删除角色和菜单权限关联表相关数据
    int removeByRoleId(String roleId);

    int batchInsertRolePermission(List<SysRolePermission> list);

    //根据角色id获取该角色关联的菜单权限id集合
    List<String> getPermissionIdsByRoleId(String roleId);


}