package com.yida.safety.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @program: safe_manage
 * @description:
 * @author: YiDa
 * @create: 2020-04-14 23:02
 **/
@Data
public class PermissionRespAllVO {

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "子集集合")
    private List<?> children;

    private String key;

    private String code;

    private String name;

    private String perms;

    private String url;

    private String component;

    private String method;

    private String pid;

    private Integer orderNum;

    private Integer type;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Integer deleted;

    private String pidName;
}
