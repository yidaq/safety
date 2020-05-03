package com.yida.safety.util;

import com.yida.safety.constants.Constant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

/**
 * @program: ssafe
 * @description: jwt工具类
 * @author: YiDa
 * @create: 2020-03-18 19:35
 **/

@Slf4j
public class JwtTokenUtil {
    private static String secretKey;
    private static Duration accessTokenExpireTime;
    private static Duration refreshTokenExpireTime;
    private static Duration refreshTokenExpireAppTime;
    private static String  issuer;

    public static void setTokenSettings(TokenSettings tokenSettings){
        secretKey=tokenSettings.getSecretKey();
        accessTokenExpireTime=tokenSettings.getAccessTokenExpireTime();
        refreshTokenExpireTime=tokenSettings.getRefreshTokenExpireTime();
        refreshTokenExpireAppTime=tokenSettings.getRefreshTokenExpireAppTime();
        issuer=tokenSettings.getIssuer();
    }
    /**
    * @Description: 生成 access_token
    * @Param: [subject, claims]
    * @return: java.lang.String
    * @Author: YiDa
    * @Date: 2020/3/18
    */
    public static String getAccessToken(String subject, Map<String,Object> claims){
        return generateToken(issuer,subject,claims,accessTokenExpireTime.toMillis(),secretKey);
    }
    /** 
    * @Description: 签发token
     * @param issuer 签发人
     * @param subject 代表这个JWT的主体，即它的所有人 一般是用户id
     * @param claims 存储在JWT里面的信息 一般放些用户的权限/角色信息
     * @param ttlMillis 有效时间(毫秒)
    * @return: java.lang.String 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static String generateToken(String issuer, String subject,Map<String, Object> claims,
                                       long ttlMillis,String secret) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        byte[] signingKey = DatatypeConverter.parseBase64Binary(secret);
        JwtBuilder builder = Jwts.builder();
        if(null!=claims){
            builder.setClaims(claims);
        }
        if (!StringUtils.isEmpty(subject)) {
            builder.setSubject(subject);
        }
        if (!StringUtils.isEmpty(issuer)) {
            builder.setIssuer(issuer);
        }
        builder.setIssuedAt(now);
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        builder.signWith(signatureAlgorithm, signingKey);
        return builder.compact();
    }
    /** 
    * @Description: 生产 PC refresh_token 
    * @Param: [subject, claims] 
    * @return: java.lang.String 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static String getRefreshToken(String subject,Map<String,Object> claims){
        return generateToken(issuer,subject,claims,refreshTokenExpireTime.toMillis(),secretKey);
    }
    /** 
    * @Description: 生产 App端 refresh_token 
    * @Param: [subject, claims] 
    * @return: java.lang.String 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static String getRefreshAppToken(String subject,Map<String,Object> claims){
        return generateToken(issuer,subject,claims,refreshTokenExpireAppTime.toMillis(),secretKey);
    }

    /** 
    * @Description: 从令牌中获取数据声明 
    * @Param: [token] 
    * @return: io.jsonwebtoken.Claims 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secretKey)).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }
    /** 
    * @Description: 获取用户id 
    * @Param: [token]
    * @return: java.lang.String 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static String getUserId(String token){
        String userId=null;
        try {
            Claims claims = getClaimsFromToken(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            log.error("eror={}",e);
        }
        return userId;
    }
    /** 
    * @Description: 获取用户名 
    * @Param: [token] 
    * @return: java.lang.String 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static String getUserName(String token){

        String username=null;
        try {
            Claims claims = getClaimsFromToken(token);
            username = (String) claims .get(Constant.JWT_USER_NAME);
        } catch (Exception e) {
            log.error("eror={}",e);
        }
        return username;
    }
    /** 
    * @Description: 检查token是否过期 
    * @Param: [token] 
    * @return: java.lang.Boolean 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static Boolean isTokenExpired(String token) {

        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("error={}",e);
            return true;
        }
    }
    /** 
    * @Description: 校验令牌 
    * @Param: [token] 
    * @return: java.lang.Boolean 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static Boolean validateToken(String token) {
        Claims claimsFromToken = getClaimsFromToken(token);
        return (null!=claimsFromToken && !isTokenExpired(token));
    }
    /** 
    * @Description: 刷新token 
    * @Param: [refreshToken, claims] 
    * @return: java.lang.String 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static String refreshToken(String refreshToken,Map<String, Object> claims) {
        String refreshedToken;
        try {
            Claims parserclaims = getClaimsFromToken(refreshToken);
            /**
             * 刷新token的时候如果为空说明原先的 用户信息不变 所以就引用上个token里的内容
             */
            if(null==claims){
                claims=parserclaims;
            }
            refreshedToken = generateToken(parserclaims.getIssuer(),parserclaims.getSubject(),claims,accessTokenExpireTime.toMillis(),secretKey);
        } catch (Exception e) {
            refreshedToken = null;
            log.error("error={}",e);
        }
        return refreshedToken;
    }
    /** 
    * @Description: 获取token剩余时间
    * @Param: [token] 
    * @return: long 
    * @Author: YiDa 
    * @Date: 2020/3/18 
    */ 
    public static long getRemainingTime(String token){
        long result=0;
        try {
            long nowMillis = System.currentTimeMillis();
            result= getClaimsFromToken(token).getExpiration().getTime()-nowMillis;
        } catch (Exception e) {
            log.error("error={}",e);
        }
        return result;
    }

}
