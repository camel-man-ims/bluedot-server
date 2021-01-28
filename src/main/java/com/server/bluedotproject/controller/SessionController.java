package com.server.bluedotproject.controller;

import com.server.bluedotproject.dto.ApiMessage;
import com.server.bluedotproject.dto.request.LoginCheckApiRequest;
import com.server.bluedotproject.dto.request.SessionApiRequest;
import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.security.JwtTokenProvider;
import com.server.bluedotproject.service.AccountService;
import com.server.bluedotproject.service.SessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "로그인")
@Slf4j
@RestController
public class SessionController {

    private final SessionService sessionService;
    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public SessionController(SessionService sessionService, AccountService accountService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.sessionService = sessionService;
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @ApiOperation(value = "회원가입 가능 이메일 체크", notes = "회원가입 가능하면 true, 불가능하면 false 반환")
    @PostMapping("/email-check")
    public ApiMessage<Map<String,Boolean>> emailCheck(@RequestBody LoginCheckApiRequest loginCheckApiRequest) {

        if(accountService.emailIsPresent(loginCheckApiRequest.getEmail())){
            return ApiMessage.RESPONSE(ApiMessage.Status.NOT_MODIFIED,createReturnResultUsingMap("isPossible",false));
        }else{
            return ApiMessage.RESPONSE(ApiMessage.Status.NOT_MODIFIED,createReturnResultUsingMap("isPossible",true));
        }
    }


    @ApiOperation(value = "로그인", notes = "insert email, password -> name, accessToken")
    @PostMapping("/login")
    public ApiMessage<Map<String,String>> loginWithCreateJWT(
            @RequestBody SessionApiRequest resource) throws URISyntaxException {

        String email = resource.getEmail();
        String password = resource.getPassword();

//        User user = sessionService.authenticate(email,password);

        /**
         * 해당 User정보를 User~Token에 주입하여 'authentication' 객체 생성
         * 해당 객체를 AuthenticationManager에게 위임
         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email,password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 토큰 생성
        String jwt = jwtTokenProvider.generateToken(authentication);

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED, createReturnResultUsingMap("token",jwt));
    }

    // <--> Method <--> //
    public static <T> Map<String,T> createReturnResultUsingMap(String insertDataAttribute, T insertData){
        Map<String,T> returnValue = new HashMap<>();

        returnValue.put(insertDataAttribute,insertData);

        return returnValue;
    }

}
