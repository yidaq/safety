package com.yida.safety.service.impl;

import com.github.pagehelper.PageHelper;
import com.yida.safety.common.BaseResponseCode;
import com.yida.safety.dao.SysLogMapper;
import com.yida.safety.exception.BusinessException;
import com.yida.safety.pojo.SysLog;
import com.yida.safety.service.LogService;
import com.yida.safety.util.PageUtil;
import com.yida.safety.vo.PageVO;
import com.yida.safety.vo.req.SysLogPageReqVO;
import com.yida.safety.vo.resp.LogChartRespVO;
import com.yida.safety.vo.resp.LogUserCountRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private SysLogMapper sysLogMapper;

    @Override
    public PageVO<SysLog> pageInfo(SysLogPageReqVO vo) {
        PageHelper.startPage(vo.getPageNum(),vo.getPageSize());
        return PageUtil.getPageVO(sysLogMapper.selectAll(vo));
    }

    @Override
    public void deletedLog(List<String> logIds) {
        int i = sysLogMapper.batchDeletedLog(logIds);
        if(i==0){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public List<LogChartRespVO> getChart() {
        List<LogChartRespVO> list = new ArrayList<LogChartRespVO>();
        for(int i = 1 ; i<13 ; i++){
            LogChartRespVO logChartRespVO = new LogChartRespVO();
            logChartRespVO.setX(i+"月");
            logChartRespVO.setY(sysLogMapper.getMonthCount(i));
            list.add(logChartRespVO);
        }
        return list;
    }

    @Override
    public List<LogUserCountRespVo> getUserLogCount() {
        return sysLogMapper.getUserLogCount();
    }


}
