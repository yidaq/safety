package com.yida.safety.util;

import com.github.pagehelper.Page;
import com.yida.safety.vo.PageVO;

import java.util.List;

/** 
* @Description: 分页工具类
* @Author: YiDa 
* @Date: 2020/3/19 
*/ 
public class PageUtil {

    private PageUtil(){

    }
    public static <T> PageVO<T> getPageVO(List<T> list){
        PageVO<T> result=new PageVO<>();
        if(list instanceof Page){
            Page<T> page= (Page<T>) list;
            result.setTotalRows(page.getTotal());
            result.setTotalPages(page.getPages());
            result.setPageNum(page.getPageNum());
            result.setCurPageSize(page.getPageSize());
            result.setPageSize(page.size());
            result.setList(page.getResult());
        }
        return result;
    }
}
