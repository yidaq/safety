package com.yida.safety.dao;

import com.yida.safety.pojo.SysRole;
import com.yida.safety.vo.req.RolePageReqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysRole record);

    int insertSelective(SysRole record);

    SysRole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysRole record);

    int updateByPrimaryKey(SysRole record);

    String getRoleNameById(String roleId);

    List<SysRole> getRoleInfoByIds(List<String> ids);

    List<SysRole> selectAll(RolePageReqVO vo);


}