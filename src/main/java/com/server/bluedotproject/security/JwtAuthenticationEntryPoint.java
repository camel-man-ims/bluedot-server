package com.server.bluedotproject.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JwtAuthenticationEntryPoint 해당 클래스는
 * 인증이 이루어지지 않은 상태에서 보호 자원에 접근 시도가 들어오면
 * 클라이언트에게 401에러를 반환하는데 사용
 * ( SpringSecurity의 AuthenticationEntryPoint사용 )
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("Responding with unAuthorized error. Message - {}", authException.getMessage());
        response.sendError(response.SC_UNAUTHORIZED, authException.getMessage());
    }
}