package com.yida.safety.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @program: safety2
 * @description:
 * @author: YiDa
 * @create: 2020-05-01 19:45
 **/
@Data
public class AddDeptUsersReqVO {

    @ApiModelProperty(value = "用户组")
    private List<String> userId;
    @ApiModelProperty(value = "部门id")
    private String deptId;
}
