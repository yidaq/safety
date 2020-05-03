package com.yida.safety.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
public class RolePageReqVO {

    @ApiModelProperty(value = "第几页")
    private int pageNum=1;
    @ApiModelProperty(value = "当前页的数量")
    private int pageSize;

    @ApiModelProperty(value = "角色id")
    private String roleId;
    @ApiModelProperty(value = "角色名称")
    private String name;
    @ApiModelProperty(value = "角色状态")
    private Integer status;
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @ApiModelProperty(value = "描述")
    private String description;

    private List<String> createTime;
}
