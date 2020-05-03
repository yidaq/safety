package com.yida.safety.vo.req;

import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * @program: safety2
 * @description:
 * @author: YiDa
 * @create: 2020-05-03 11:47
 **/
@Data
public class GetDeptPermissionsRepVO {
    private String deptId;
    private String userId;
}
