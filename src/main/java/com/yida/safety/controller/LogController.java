package com.yida.safety.controller;


import com.yida.safety.aop.annotation.MyLog;
import com.yida.safety.common.DataResult;
import com.yida.safety.pojo.SysLog;
import com.yida.safety.service.LogService;
import com.yida.safety.vo.PageVO;
import com.yida.safety.vo.req.SysLogPageReqVO;
import com.yida.safety.vo.resp.LogChartRespVO;
import com.yida.safety.vo.resp.LogUserCountRespVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@Api(tags = "系统管理-日志管理",description = "日志管理相关接口")
public class LogController {
    @Autowired
    private LogService logService;

    @PostMapping("/logs")
    @RequiresPermissions("sys:log:list")
    public DataResult<PageVO<SysLog>> pageInfo(@RequestBody SysLogPageReqVO vo){
        PageVO<SysLog> sysLogPageVO = logService.pageInfo(vo);
        DataResult result=DataResult.success();
        result.setData(sysLogPageVO);
        return result;
    }
    @DeleteMapping("/log")
    @ApiOperation(value = "删除日志接口")
    @RequiresPermissions("sys:log:delete")
    public DataResult deletedLog(@RequestBody @ApiParam(value = "日志id集合") List<String> logIds){
        logService.deletedLog(logIds);
        DataResult result=DataResult.success();
        return result;
    }

    @GetMapping("/log/chart")
    @ApiOperation(value = "获取日志图表数据")
//    @RequiresPermissions("sys:log:list")
    public DataResult<List<LogChartRespVO>> getChart(){
        List<LogChartRespVO> list = logService.getChart();
        DataResult result=DataResult.success();
        result.setData(list);
        return result;
    }

    @GetMapping("/log/count")
    @ApiOperation(value = "获取用户操作日志")
//    @RequiresPermissions("sys:log:list")
    public DataResult<List<LogUserCountRespVo>> getUserLogCount(){
        List<LogUserCountRespVo> list = logService.getUserLogCount();
        DataResult result=DataResult.success();
        result.setData(list);
        return result;
    }
}
