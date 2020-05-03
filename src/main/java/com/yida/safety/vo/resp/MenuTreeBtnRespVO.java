package com.yida.safety.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-27 11:57
 **/
@Data
public class MenuTreeBtnRespVO {

    @ApiModelProperty(value = "主键id")
    private String key;
    @ApiModelProperty(value = "树名称")
    private String title;
    @ApiModelProperty(value = "树值")
    private String value;
    @ApiModelProperty(value = "节点是否选中")
    private boolean checked;

}
