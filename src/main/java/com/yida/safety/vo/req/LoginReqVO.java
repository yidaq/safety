package com.yida.safety.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @program: ssafe
 * @description: 接收客户端请求进来数据
 * @author: YiDa
 * @create: 2020-03-18 19:57
 **/

@Data
public class LoginReqVO {
    @ApiModelProperty(value = "账号")
    private String username;
    @ApiModelProperty(value = "用户密码")
    private String password;
    @ApiModelProperty(value = "登录类型(1:pc;2:App)")
    @NotBlank(message = "登录类型不能为空")
    private String type;
}
