package com.yida.safety.service;

import com.yida.safety.pojo.SysDept;
import com.yida.safety.pojo.SysPermission;
import com.yida.safety.pojo.SysUser;
import com.yida.safety.vo.req.*;
import com.yida.safety.vo.resp.DeptOwnPermissionRespVO;
import com.yida.safety.vo.resp.DeptTreeRespVO;
import com.yida.safety.vo.resp.SelectUserRespVO;
import com.yida.safety.vo.resp.UserOwnRoleRespVO;

import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-28 11:49
 **/

public interface DeptService {

    List<SysDept> selectAll();

    List<DeptTreeRespVO> deptTreeList(String deptId);

    UserOwnRoleRespVO getDeptOwnRole(String deptId);

    void setDeptOwnRole(DeptOwnRoleReqVO vo);

    SysDept addDept(DeptAddReqVO vo);

    List<SysUser> getUsersByDeptId(String deptId);

    List<SysPermission> getDeptPermission(String deptId);

    SysDept getDeptInfoById(String deptid);

    List<SelectUserRespVO> getExDeptUser();

    void setDeptUsers (AddDeptUsersReqVO vo);

    void deleteDeptUser(DeleteDeptUser deleteDeptUser);

    void deletedDept(String id);

    DeptOwnPermissionRespVO getDeptPermissions(String deptId,String userId);

    void updateUserPermission(DeptUpdateUserPeEeqVO vo);
}
