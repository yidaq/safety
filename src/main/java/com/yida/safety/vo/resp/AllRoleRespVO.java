package com.yida.safety.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-28 14:44
 **/
@Data
public class AllRoleRespVO {

    @ApiModelProperty(value = "key")
    private String key;
    @ApiModelProperty(value = "title")
    private String title;
    @ApiModelProperty(value = "disabled")
    private boolean disabled;
}
