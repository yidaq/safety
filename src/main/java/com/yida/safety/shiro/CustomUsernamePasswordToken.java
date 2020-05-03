package com.yida.safety.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * @program: ssafe
 * @description: 改写UsernamePasswordToken类
 * @author: YiDa
 * @create: 2020-03-19 10:17
 **/

public class CustomUsernamePasswordToken extends UsernamePasswordToken {

    private String token;

    public CustomUsernamePasswordToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }
}

