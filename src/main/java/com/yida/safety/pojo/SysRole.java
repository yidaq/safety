package com.yida.safety.pojo;

import com.yida.safety.vo.resp.MenuTreeRespVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SysRole implements Serializable {
    private String id;

    private String name;

    private String description;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Integer deleted;

    private List<MenuTreeRespVO> menuTreeRespVO;

    private List<String> permissions;

}