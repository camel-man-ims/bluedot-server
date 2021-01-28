package com.server.bluedotproject.config;

import com.server.bluedotproject.security.JwtAuthenticationEntryPoint;
import com.server.bluedotproject.security.JwtFilter;
import com.server.bluedotproject.security.JwtTokenProvider;
import com.server.bluedotproject.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableGlobalMethodSecurity(
        securedEnabled = true,
        prePostEnabled = true,
        jsr250Enabled = true
)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final SessionService sessionService;
    private final JwtAuthenticationEntryPoint unAuthorizedHandler;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.ExpirationInMs}")
    private int jwtExpInMs;


    @Bean
    public JwtTokenProvider jwtTokenProvider(){
        return new JwtTokenProvider(secret,jwtExpInMs);
    }

    @Bean
    public JwtFilter jwtFilter(){
        return new JwtFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception{
        authenticationManagerBuilder
                .userDetailsService(sessionService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable() // API 서버이므로 기본 설정 필요 없음 (기본 설정은 비인증시 로그인 폼 화면으로 리다이렉트)
                .csrf().disable()  // API 서버이므로 csrf보안 필요없음 (폼 데이터 암호화 처리)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 토큰으로 인증, 인가가 이루어지므로 session 필요 없음 (stateless 방식)
                .and()
                .cors().disable()
                .formLogin().disable()
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedHandler)
                .and()
                .headers().frameOptions().disable()
                .and()
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
