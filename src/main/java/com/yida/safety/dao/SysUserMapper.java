package com.yida.safety.dao;

import com.yida.safety.pojo.SysUser;
import com.yida.safety.vo.req.UserPageReqVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    SysUser selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);

    SysUser getUserInfoByName(String username);

    List<SysUser> selectAll(UserPageReqVO vo);

    int deletedUsers(@Param("sysUser") SysUser sysUser, @Param("list") List<String> list);

    List<SysUser> getUserByKey(@Param("key") String key);

    List<SysUser> getDeptUsers(List<String> list);

    List<SysUser> getExtIds(List<String> list);

    List<SysUser> getAll();

    int updateDeptId(@Param("userId") String userId,@Param("deptId") String deptId);

}