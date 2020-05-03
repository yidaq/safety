package com.yida.safety.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @program: safety2
 * @description:
 * @author: YiDa
 * @create: 2020-05-02 10:49
 **/
@Data
public class DeleteDeptUser {
    @ApiModelProperty(value = "用户id")
    private String userId;
    @ApiModelProperty(value = "部门id")
    private String deptId;
}
