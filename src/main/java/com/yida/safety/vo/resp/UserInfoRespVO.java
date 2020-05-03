package com.yida.safety.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/** 
* @Description: 用户信息vo
* @Param:  
* @return:  
* @Author: YiDa 
* @Date: 2020/4/2 
*/ 
@Data
public class UserInfoRespVO {
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "账号")
    private String username;
    @ApiModelProperty(value = "部门id")
    private String deptId;
    @ApiModelProperty(value = "所属部门名称")
    private String group;
    @ApiModelProperty(value = "昵称")
    private String name;
    @ApiModelProperty(value = "头像地址")
    private String avatar;
    @ApiModelProperty(value = "手机号")
    private String phone;
    @ApiModelProperty(value = "邮箱")
    private String email;
    @ApiModelProperty(value = "角色")
    private List<String> role;

}
