package com.yida.safety.dao;

import com.yida.safety.pojo.SysUserDept;
import com.yida.safety.vo.req.DeleteDeptUser;

import java.util.List;

public interface SysUserDeptMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysUserDept record);

    int insertSelective(SysUserDept record);

    SysUserDept selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysUserDept record);

    int updateByPrimaryKey(SysUserDept record);

    List<String> getDeptIdsByUserId(String userId);

    List<String> getUserIdByDeptId(String deptId);

    List<String> getRoleIdsByUserId(String userId);

    List<SysUserDept> selectAll();

    int deleteKeyByUserId(String userId);

    int deleteKeyByDeptUserId(DeleteDeptUser deleteDeptUser);

    List<String> selectUserInfoByDeptIds(List<String> list);

    String selectIdByUserId(String userId);
}