package com.yida.safety.util;

import org.springframework.stereotype.Component;

/**
 * @program: ssafe
 * @description: jwt初始化配置类
 * @author: YiDa
 * @create: 2020-03-18 19:40
 **/

@Component
public class InitializerUtil {
    private TokenSettings tokenSettings;

    public InitializerUtil(TokenSettings tokenSettings) {
        JwtTokenUtil.setTokenSettings(tokenSettings);
    }

}
