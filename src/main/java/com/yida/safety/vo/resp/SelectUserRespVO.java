package com.yida.safety.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @program: safety2
 * @description:
 * @author: YiDa
 * @create: 2020-05-01 13:06
 **/
@Data
public class SelectUserRespVO {

    @ApiModelProperty(value = "用户名称")
    private String title;
    @ApiModelProperty(value = "值")
    private String value;
}
