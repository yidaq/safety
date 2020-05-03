package com.yida.safety.controller;

import com.sun.org.glassfish.gmbal.ParameterNames;
import com.yida.safety.aop.annotation.MyLog;
import com.yida.safety.common.BaseResponseCode;
import com.yida.safety.common.DataResult;
import com.yida.safety.constants.Constant;
import com.yida.safety.pojo.SysUser;
import com.yida.safety.service.UserService;
import com.yida.safety.util.JwtTokenUtil;
import com.yida.safety.vo.PageVO;
import com.yida.safety.vo.req.*;
import com.yida.safety.vo.resp.LoginRespVO;
import com.yida.safety.vo.resp.SelectUserRespVO;
import com.yida.safety.vo.resp.UserInfoRespVO;
import com.yida.safety.vo.resp.UserOwnRoleRespVO;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-24 11:33
 **/

@RestController
@Api(tags = "组织管理-用户管理")
@RequestMapping("/api")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/login")
    @ApiOperation(value = "用户登录接口")
    public DataResult<LoginRespVO> login(@RequestBody LoginReqVO vo){
        DataResult<LoginRespVO> result=DataResult.success();
        result.setData(userService.login(vo));
        return result;
    }

    @GetMapping("/user/logout")
    @ApiOperation(value = "用户登出接口")
    public DataResult logout(HttpServletRequest request){
        try {
            String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
            String refreshToken=request.getHeader(Constant.REFRESH_TOKEN);
            userService.logout(accessToken,refreshToken);
        } catch (Exception e) {
            log.error("logout error{}",e);
        }
        return DataResult.success("退出成功");
    }

    @GetMapping("/user/token")
    @ApiOperation(value = "jwt token 刷新接口")
    public DataResult<String> refreshToken(HttpServletRequest request){
        String refreshToken=request.getHeader(Constant.REFRESH_TOKEN);
        String newAccessToken = userService.refreshToken(refreshToken);
        DataResult result=DataResult.success();
        result.setData(newAccessToken);
        return result;
    }


    @GetMapping("/user/currentUser")
    @ApiOperation(value = "获取当前用户")
    public DataResult<UserInfoRespVO> getHome(HttpServletRequest request){
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String userId = JwtTokenUtil.getUserId(accessToken);
        UserInfoRespVO userInfoRespVO = userService.getCurrentUser(userId);
        return DataResult.success(userInfoRespVO);
    }

    @PostMapping("/users")
    @ApiOperation(value = "分页查询用户接口")
    @MyLog(title = "组织管理-用户管理",action = "分页查询用户接口")
    @RequiresPermissions("sys:user:list")
    public DataResult<PageVO<SysUser>> pageInfo(@RequestBody UserPageReqVO vo){
        if(vo.getCreateTime() != null){
            vo.setStartTime(vo.getCreateTime().get(0));
            vo.setEndTime(vo.getCreateTime().get(1));
        }
        DataResult result=DataResult.success();
        result.setData(userService.pageInfo(vo));
        return result;
    }

    @PostMapping("/user")
    @ApiOperation(value = "新增用户接口")
    @MyLog(title = "组织管理-用户管理",action = "新增用户接口")
    @RequiresPermissions("sys:user:add")
    public DataResult addUser(@RequestBody @Valid UserAddReqVO vo){
        DataResult result=DataResult.success();
        userService.addUser(vo);
        return result;
    }

    @PostMapping("/user/roles")
    @ApiOperation(value = "查询用户拥有的角色数据接口")
    @MyLog(title = "组织管理-用户管理",action = "查询用户拥有的角色数据接口")
    @RequiresPermissions("sys:user:role:update")
    public DataResult<UserOwnRoleRespVO> getUserOwnRole(String userId){
        DataResult result=DataResult.success();
        result.setData(userService.getUserOwnRole(userId));
        return result;
    }

    @PutMapping("/user/roles")
    @ApiOperation(value = "保存用户拥有的角色信息接口")
    @MyLog(title = "组织管理-用户管理",action = "保存用户拥有的角色信息接口")
    @RequiresPermissions("sys:user:role:update")
    public DataResult saveUserOwnRole(@RequestBody @Valid UserOwnRoleReqVO vo){
        DataResult result=DataResult.success();
        userService.setUserOwnRole(vo);
        return result;
    }

    @DeleteMapping("/user")
    @ApiOperation(value = "批量/删除用户接口")
    @MyLog(title = "组织管理-用户管理",action = "批量/删除用户接口")
    @RequiresPermissions("sys:user:delete")
    public DataResult deletedUsers(@RequestBody @ApiParam(value = "用户id集合") List<String> list, HttpServletRequest request){
        String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
        String operationId=JwtTokenUtil.getUserId(accessToken);
        userService.deletedUsers(list,operationId);
        DataResult result=DataResult.success();
        return result;
    }

    @PutMapping("/user")
    @ApiOperation(value ="列表修改用户信息接口")
    @MyLog(title = "组织管理-用户管理",action = "列表修改用户信息接口")
    @RequiresPermissions("sys:user:update")
    public DataResult updateUserInfo(@RequestBody @Valid UserUpdateReqVO vo, HttpServletRequest request){
        String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
        String userId= JwtTokenUtil.getUserId(accessToken);
        DataResult result=DataResult.success();
        userService.updateUserInfo(vo,userId);
        return result;
    }

    @PutMapping("/user/info")
    @ApiOperation(value = "保存个人信息接口")
    @MyLog(title = "组织管理-用户管理",action = "保存个人信息接口")
    public DataResult<UserInfoRespVO> saveUserInfo(@RequestBody UserUpdateDetailInfoReqVO vo,HttpServletRequest request){
        String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
        String id=JwtTokenUtil.getUserId(accessToken);
        UserInfoRespVO userInfoRespVO =  userService.userUpdateDetailInfo(vo,id);
        DataResult result=DataResult.success(userInfoRespVO);
        return result;
    }

    @PutMapping("/user/pwd")
    @ApiOperation(value = "修改个人密码接口")
    public DataResult updatePwd(@RequestBody @Valid UserUpdatePwdReqVO vo,HttpServletRequest request){
        String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
        String refresgToken=request.getHeader(Constant.REFRESH_TOKEN);
        userService.userUpdatePwd(vo,accessToken,refresgToken);
        DataResult result=DataResult.success();
        return result;
    }

    @GetMapping("/user/loginUsers")
    @ApiOperation(value = "获取在线用户")
    public DataResult<List<SysUser>> getLoginUser(HttpServletRequest request){
        List<SysUser> userList =  userService.getLoginUser();
        return DataResult.success(userList);
    }

    @PostMapping("/user/selectUser")
    @ApiOperation(value = "模糊查询用户接口")
    @MyLog(title = "组织管理-用户管理",action = "模糊查询用户接口")
    public DataResult<List<SelectUserRespVO>> getUserByKey(@RequestBody SelectReqKey selectReqKey){
        DataResult result=DataResult.success();
        result.setData(userService.getUserByKey(selectReqKey.getKey()));
        return result;
    }

    @GetMapping("/user/permissions")
    @ApiOperation(value = "获取用户自己访问权限")
    @MyLog(title = "组织管理-用户管理",action = "获取用户自己访问权限")
    public DataResult<List<String>> getUserOwnPermission(HttpServletRequest request){
        String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
        String userId= JwtTokenUtil.getUserId(accessToken);
        DataResult result=DataResult.success(userService.getUserOwnPermisson(userId));
        return result;
    }

}
