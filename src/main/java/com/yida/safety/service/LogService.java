package com.yida.safety.service;



import com.yida.safety.pojo.SysLog;
import com.yida.safety.vo.PageVO;
import com.yida.safety.vo.req.SysLogPageReqVO;
import com.yida.safety.vo.resp.LogChartRespVO;
import com.yida.safety.vo.resp.LogUserCountRespVo;

import java.util.List;


public interface LogService {

    PageVO<SysLog> pageInfo(SysLogPageReqVO vo);

    void deletedLog(List<String> logIds);

    List<LogChartRespVO> getChart();

    List<LogUserCountRespVo> getUserLogCount();
}
