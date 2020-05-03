package com.yida.safety.dao;

import com.yida.safety.pojo.SysLog;
import com.yida.safety.vo.req.SysLogPageReqVO;
import com.yida.safety.vo.resp.LogUserCountRespVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysLogMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysLog record);

    int insertSelective(SysLog record);

    SysLog selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysLog record);

    int updateByPrimaryKey(SysLog record);

    List<SysLog> selectAll(SysLogPageReqVO vo);

    int batchDeletedLog(List<String> logIds);

    Integer getMonthCount(Integer mouth);

    List<LogUserCountRespVo> getUserLogCount();
}