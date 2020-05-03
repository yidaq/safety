package com.yida.safety.vo.resp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @program: safety2
 * @description:
 * @author: YiDa
 * @create: 2020-05-03 11:27
 **/
@Data
@Api(value = "部门资源信息返回VO")
public class DeptOwnPermissionRespVO {

    @ApiModelProperty(value = "拥有资源集合")
    private List<String> ownPermissions;

    @ApiModelProperty(value = "所有资源列表")
    private List<AllRoleRespVO> allPermissions;
}
