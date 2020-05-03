package com.yida.safety.controller;

import com.yida.safety.aop.annotation.MyLog;
import com.yida.safety.common.DataResult;
import com.yida.safety.constants.Constant;
import com.yida.safety.pojo.SysPermission;
import com.yida.safety.service.PermissionService;
import com.yida.safety.util.JwtTokenUtil;
import com.yida.safety.vo.req.PermissionAddReqVO;
import com.yida.safety.vo.req.PermissionUpdateReqVO;
import com.yida.safety.vo.resp.MenuRespVO;
import com.yida.safety.vo.resp.MenuTreeRespVO;
import com.yida.safety.vo.resp.PermissionRespAllVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-24 17:45
 **/
@RestController
@RequestMapping("/api")
@Api(tags = "组织管理-菜单权限管理",description = "菜单权限管理相关接口")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/permissions/getMenu")
    @ApiOperation(value = "根据userId获取菜单")
    public DataResult<List<MenuRespVO>> getAllMenu(HttpServletRequest request){
        String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
        String userId= JwtTokenUtil.getUserId(accessToken);
        DataResult result=DataResult.success();
        result.setData(permissionService.getMenu(userId));
        return result;
    }

    @GetMapping("/permissions/getPermission")
    @ApiOperation(value = "获取所有的菜单权限数据接口")
    @MyLog(title = "组织管理-菜单权限管理",action = "获取所有的菜单权限数据接口")
    public DataResult<List<MenuRespVO>> getAllPermission(){
        DataResult result=DataResult.success();
        result.setData(permissionService.getPermission());
        return result;
    }

    @GetMapping("/permission/getPermissionTable")
    @ApiOperation(value = "获取所有权限树接口")
    @RequiresPermissions("sys:permission:list")
    public DataResult<List<PermissionRespAllVO>> getPermissionTable() {
        DataResult<List<PermissionRespAllVO>> result = DataResult.success();
        result.setData(permissionService.selectPermissionTable());
        return result;
    }

    @PostMapping("/permission/add")
    @ApiOperation(value = "新增菜单权限接口")
    @MyLog(title = "组织管理-菜单权限管理",action = "新增菜单权限接口")
    @RequiresPermissions("sys:permission:add")
    public DataResult<SysPermission> addPermission(@RequestBody @Valid PermissionAddReqVO vo){
        DataResult result=DataResult.success();
        result.setData(permissionService.addPermission(vo));
        return result;
    }

    @PostMapping("/permission/getMenuTree")
    @ApiOperation(value = "获取菜单树")
    @RequiresPermissions(value = {"sys:role:update","sys:role:add"},logical = Logical.OR)
    public DataResult<List<MenuTreeRespVO>> getMenuTree(String type){
        DataResult<List<MenuTreeRespVO>> result=DataResult.success();
        result.setData(permissionService.selectMenuTree(type));
        return result;
    }

    @DeleteMapping("/permission")
    @ApiOperation(value = "删除菜单权限接口")
    @MyLog(title = "组织管理-菜单权限管理",action = "删除菜单权限接口")
    @RequiresPermissions("sys:permission:delete")
    public DataResult deletedPermission(String permissionId){
        DataResult result=DataResult.success();
        permissionService.deletedPermission(permissionId);
        return result;
    }

    @PutMapping("/permission")
    @ApiOperation(value = "编辑菜单权限接口")
    @MyLog(title = "组织管理-菜单权限管理",action = "编辑菜单权限接口")
    @RequiresPermissions("sys:permission:update")
    public DataResult updatePermission(@RequestBody @Valid PermissionUpdateReqVO vo){
        permissionService.updatePermission(vo);
        DataResult result=DataResult.success();
        return result;
    }






}
