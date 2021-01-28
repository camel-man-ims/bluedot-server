package com.server.bluedotproject.security;

import com.server.bluedotproject.exceptions.AuthRNotAllowedException;
import com.server.bluedotproject.exceptions.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtTokenProvider {

    private Key key;
    private Date expiryDate;


    public JwtTokenProvider(String secret, int jwtExpInMs){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        Date time = new Date();
        expiryDate = new Date(time.getTime() + jwtExpInMs);
    }

    public String generateToken(Authentication authentication){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return Jwts.builder()
                .setHeader(createHeader())
                .setClaims(createClaims(userPrincipal))
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * JWT Token에서 user.id 정보 가져오기
     * @param token
     * @return
     */
    public Long getUserIdFromJwt(String token){

        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();

//        log.info("userId from jwt : " + claims.getSubject());
        log.info("get userId from jwt : " + claims.getSubject());

        return Long.parseLong(claims.getSubject());
    }

    /**
     * 유효한 토큰인지 검사
     * @param authToken
     * @return
     */
    public boolean validateToken(String authToken){
        try{

            log.info("validate token : " + authToken);
            Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(authToken)
                    .getBody();
            return true;
        }

        // TODO : Test Code 작성
        catch (SignatureException ex){
            throw new AuthRNotAllowedException(ErrorCode.INVALID_JWT_SIGNATURE);
        }catch (MalformedJwtException ex){
            throw new AuthRNotAllowedException(ErrorCode.INVALID_JWT_TOKEN);
        }catch (ExpiredJwtException ex){
            throw new AuthRNotAllowedException(ErrorCode.EXPIRED_JWT_TOKEN);
        }catch (UnsupportedJwtException ex){
            throw new AuthRNotAllowedException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        }catch (IllegalArgumentException ex) {
            throw new AuthRNotAllowedException(ErrorCode.JWT_CLAIMS_IS_EMPTY);
        }

    }



    /**
     * token setting  : header
     */
    private static Map<String, Object> createHeader(){
        Map<String, Object> header = new HashMap<>();

        header.put("regDate", System.currentTimeMillis());
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        return header;
    }

    /**
     * token setting  : claims
     */
    private Map<String, Object> createClaims(UserPrincipal user){
        Map<String, Object> claims = new HashMap<>();

        claims.put("role", user.getAuthorities());
        claims.put("email", user.getEmail());

        return claims;

    }

}
