package com.yida.safety.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class UserUpdateDetailInfoReqVO {
    @ApiModelProperty(value = "邮箱")
    private String email;
    @ApiModelProperty(value = "真实名称")
    private String nickName;
    @ApiModelProperty(value = "手机号")
    private String phone;

}
