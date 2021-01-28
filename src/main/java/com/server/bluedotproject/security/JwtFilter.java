package com.server.bluedotproject.security;

import com.server.bluedotproject.exceptions.AuthRNotAllowedException;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private SessionService sessionService;

    /**
     * Request로 들어오는 Jwt Token 유효성 검증
     * WebSecurityConfig - filter chain에 등록
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            String jwt = getJwtFromRequest(request);

            if(StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)){

                Long userId = jwtTokenProvider.getUserIdFromJwt(jwt);

                //userId로 DB에 조회하여 해당 User 정보 꺼내기
                UserDetails userDetails = sessionService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication
                        = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Security Context에 인증 정보 조회
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (Exception ex){
            // Security Context에서 유저 인증 실패
            log.error("Could not set user authentication in security context ", ex);
            throw new AuthRNotAllowedException(ErrorCode.USER_NOT_AUTHORIZED);
        }

        filterChain.doFilter(request,response);
    }


    /**
     * 토큰 추출하기 (Auhorization Bearer :)제거
     * @param request
     * @return
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7, bearerToken.length());
        }

        return null;
    }


}
