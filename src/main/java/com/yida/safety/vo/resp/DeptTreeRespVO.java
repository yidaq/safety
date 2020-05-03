package com.yida.safety.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeptTreeRespVO {

    @ApiModelProperty(value = "部门id")
    private String key;

    @ApiModelProperty(value = "部门名称")
    private String title;

    @ApiModelProperty(value = "树值")
    private String value;

    @ApiModelProperty(value = "子集叶子节点")
    private List<?> children;
}
