package com.yida.safety.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-29 20:10
 **/
@Data
public class DeptOwnRoleReqVO {

    @ApiModelProperty(value = "用户id")
    @NotBlank(message = "用户id不能为空")
    private String deptId;
    @ApiModelProperty("赋予用户的角色id集合")
    private List<String> roleIds;
}
