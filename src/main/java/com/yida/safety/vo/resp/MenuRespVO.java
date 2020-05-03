package com.yida.safety.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @program: safety
 * @description: menuVO
 * @author: YiDa
 * @create: 2020-04-24 17:05
 **/
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuRespVO {

    @ApiModelProperty(value = "路径")
    private String path;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "权限集合")
    private List<String> authority;
    @ApiModelProperty(value = "子集集合")
    private List<?> routes;
    @ApiModelProperty(value = "图标")
    private String icon;
    @ApiModelProperty(value = "是否绝对定位")
    private boolean exact = true;

}
