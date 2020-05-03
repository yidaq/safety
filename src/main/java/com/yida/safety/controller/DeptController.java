package com.yida.safety.controller;

import com.yida.safety.aop.annotation.MyLog;
import com.yida.safety.common.DataResult;
import com.yida.safety.pojo.SysDept;
import com.yida.safety.pojo.SysPermission;
import com.yida.safety.pojo.SysUser;
import com.yida.safety.service.DeptService;
import com.yida.safety.vo.req.*;
import com.yida.safety.vo.resp.DeptOwnPermissionRespVO;
import com.yida.safety.vo.resp.DeptTreeRespVO;
import com.yida.safety.vo.resp.SelectUserRespVO;
import com.yida.safety.vo.resp.UserOwnRoleRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.xml.crypto.Data;
import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-28 11:50
 **/
@RestController
@RequestMapping("/api")
@Api(tags = "组织管理-部门管理",description = "部门管理相关接口")
public class DeptController {

    @Autowired
    private DeptService deptService;

    @GetMapping("/depts")
    @ApiOperation(value = "查询所有部门数据接口")
    @MyLog(title = "组织管理-部门管理",action = "查询所有部门数据接口")
    public DataResult<List<SysDept>> getAllDept(){
        DataResult result =DataResult.success();
        result.setData(deptService.selectAll());
        return result;
    }

    @GetMapping("/dept/tree")
    @ApiOperation(value = "部门树形结构列表接口")
    @MyLog(title = "组织管理-部门管理",action = "部门树形结构列表接口")
    @RequiresPermissions(value = {"sys:user:update","sys:user:add","sys:dept:add","sys:dept:update"},logical = Logical.OR)
    public DataResult<List<DeptTreeRespVO>> getDeptTree(@RequestParam(required = false) String deptId){
        DataResult result=DataResult.success();
        result.setData(deptService.deptTreeList(deptId));
        return result;
    }

    @PostMapping("/dept/roles")
    @ApiOperation(value = "查询部门拥有的角色数据接口")
    @MyLog(title = "组织管理-部门管理",action = "查询部门拥有的角色数据接口")
    @RequiresPermissions("sys:dept:role:update")
    public DataResult<UserOwnRoleRespVO> getDeptOwnRole(String deptId){
        DataResult result=DataResult.success();
        result.setData(deptService.getDeptOwnRole(deptId));
        return result;
    }

    @PutMapping("/dept/roles")
    @ApiOperation(value = "保存部门拥有的角色信息接口")
    @MyLog(title = "组织管理-部门管理",action = "保存部门拥有的角色信息接口")
    @RequiresPermissions("sys:dept:role:update")
    public DataResult saveUserOwnRole(@RequestBody @Valid DeptOwnRoleReqVO vo){
        DataResult result=DataResult.success();
        deptService.setDeptOwnRole(vo);
        return result;
    }

    @PostMapping("/dept")
    @ApiOperation(value = "新增部门数据接口")
    @MyLog(title = "组织管理-部门管理",action = "新增部门数据接口")
    @RequiresPermissions("sys:dept:add")
    public DataResult<SysDept> addDept(@RequestBody @Valid DeptAddReqVO vo){
        DataResult result=DataResult.success();
        result.setData(deptService.addDept(vo));
        return result;
    }

    @PostMapping("/dept/getUsers")
    @ApiOperation(value = "获取部门成员")
    @MyLog(title = "组织管理-部门管理",action = "获取部门成员")
    public DataResult<List<SysUser>> getUsersByDeptId(String deptId){
        DataResult result=DataResult.success();
        result.setData(deptService.getUsersByDeptId(deptId));
        return result;
    }

    @PostMapping("/dept/getPermissions")
    @ApiOperation(value = "获取部门权限")
    @MyLog(title = "组织管理-部门管理",action = "获取部门权限")
    public DataResult<List<SysPermission>> getPermissionsByDeptId(String deptId){
        DataResult result=DataResult.success();
        result.setData(deptService.getDeptPermission(deptId));
        return result;
    }

    @PostMapping("/dept/deptInfo")
    @ApiOperation(value = "获取部门详细信息")
    @MyLog(title = "组织管理-部门管理",action = "获取部门详细信息")
    public DataResult<SysDept> getDeptInfoById(String deptId){
        DataResult result=DataResult.success();
        result.setData(deptService.getDeptInfoById(deptId));
        return result;
    }

    @GetMapping("/dept/extUser")
    @ApiOperation(value = "获取没有部门的用户")
    @MyLog(title = "组织管理-部门管理",action = "获取没有部门的用户")
    public DataResult<List<SelectUserRespVO>> getExtUser(){
        DataResult result=DataResult.success();
        result.setData(deptService.getExDeptUser());
        return result;
    }

    @PutMapping("/dept/deptUsers")
    @ApiOperation(value = "添加部门用户")
    @MyLog(title = "组织管理-部门管理",action = "添加部门用户")
    public DataResult saveUserOwnRole(@RequestBody AddDeptUsersReqVO vo){
        DataResult result=DataResult.success();
        deptService.setDeptUsers(vo);
        return result.success();
    }

    @DeleteMapping("/dept/deptUsers")
    @ApiOperation(value = "删除部门用户")
    @MyLog(title = "组织管理-部门管理",action = "删除部门用户")
    public DataResult deleteDeptUserById(@RequestBody DeleteDeptUser vo){
        DataResult result=DataResult.success();
        deptService.deleteDeptUser(vo);
        return result.success();
    }

    @DeleteMapping("/dept")
    @ApiOperation(value = "删除部门接口")
    @MyLog(title = "组织管理-部门管理",action = "删除部门接口")
    @RequiresPermissions("sys:dept:delete")
    public DataResult deletedDepts(String id){
        deptService.deletedDept(id);
        DataResult result=DataResult.success();
        return result;
    }

    @PostMapping("/depts/ownPermission")
    @ApiOperation(value = "获取部门权限资源接口")
    @MyLog(title = "组织管理-部门管理",action = "获取部门权限资源接口")
    public DataResult<DeptOwnPermissionRespVO> getDeptPermission(@RequestBody GetDeptPermissionsRepVO vo){
        DataResult result = DataResult.success(deptService.getDeptPermissions(vo.getDeptId(),vo.getUserId()));
        return result;
    }

}
