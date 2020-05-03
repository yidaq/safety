package com.yida.safety.vo.req;

import lombok.Data;

import java.util.List;

/**
 * @program: safety2
 * @description:
 * @author: YiDa
 * @create: 2020-05-03 13:28
 **/
@Data
public class DeptUpdateUserPeEeqVO {

    private String userId;
    private String deptId;
    private List<String> permissionId;
}
