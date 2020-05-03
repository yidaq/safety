package com.yida.safety.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: YiDa
 * @create: 2020-04-15 21:49
 **/
@Data
public class MenuTreeRespVO {

    @ApiModelProperty(value = "主键id")
    private String key;
    @ApiModelProperty(value = "树名称")
    private String title;
    @ApiModelProperty(value = "树值")
    private String value;
    @ApiModelProperty(value = "子集集合")
    private List<?> children;

}
