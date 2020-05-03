package com.yida.safety.service;

import com.yida.safety.pojo.SysRole;
import com.yida.safety.vo.PageVO;
import com.yida.safety.vo.req.AddRoleReqVO;
import com.yida.safety.vo.req.RolePageReqVO;
import com.yida.safety.vo.req.RoleUpdateReqVO;

import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-25 16:30
 **/
public interface RoleService {

    PageVO<SysRole> pageInfo(RolePageReqVO vo);
    SysRole addRole(AddRoleReqVO vo);
    List<SysRole> selectAll();
    List<String> getRoleNames(String userId);
    List<SysRole> getRoleInfoByUserId(String userId);
    void deletedRole(String roleId);
    SysRole detailInfo(String id);
    void updateRole(RoleUpdateReqVO vo);


}
