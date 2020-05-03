package com.yida.safety.dao;

import com.yida.safety.pojo.SysDeptRole;
import com.yida.safety.pojo.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDeptRoleMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysDeptRole record);

    int insertSelective(SysDeptRole record);

    SysDeptRole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysDeptRole record);

    int updateByPrimaryKey(SysDeptRole record);

    List<String> getRoleIdsByDeptId(String deptId);

    int removeRoleByDeptId(String deptId);

    int batchInsertDeptRole(List<SysDeptRole> list);

    String selectBydeptId(String deptId);

    List<String> selectDeptIdByRoleId(String roleId);

    int removeKeyByRoleId(String roleId);

}