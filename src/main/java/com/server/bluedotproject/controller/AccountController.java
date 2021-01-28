package com.server.bluedotproject.controller;

import com.server.bluedotproject.dto.ApiMessage;
import com.server.bluedotproject.dto.request.EmailConfirmApiRequest;
import com.server.bluedotproject.dto.request.AccountApiRequest;
import com.server.bluedotproject.dto.request.SessionApiRequest;
import com.server.bluedotproject.dto.response.EmailApiResponse;
import com.server.bluedotproject.service.AccountService;
import com.server.bluedotproject.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Api(tags = "회원가입/ 이메일인증/ 계정관리")
public class AccountController {


    private final AccountService accountService;

//    @ApiOperation(value = "회원가입", notes = "insert email, password, name, nickname")
//    @PostMapping("/signup")
//    public ApiMessage<Map<String,Long>> signUp(@Valid @RequestBody AccountApiRequest request) {
//
//        Map<String,Long> result = new HashMap<>();
//
//        Long userId  = accountService.create(request);
//
//        log.info("new User Id :" + userId);
//
//        result.put("userId", userId);
//
//        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,result);
//    }

    /**
     * email 인증 -> 토큰 발생 및 이메일로 전송
     */
    @ApiOperation(value = "이메일 인증 보내기", notes = "insert email")
    @PostMapping("/send-email-token")
    public ApiMessage<EmailApiResponse> sendEmailToken(@RequestBody SessionApiRequest request){

        String email = request.getEmail();

        EmailApiResponse res = accountService.sendEmailConfirmToken(email);

        log.info("send to :" + request.getEmail());

        return ApiMessage.RESPONSE(ApiMessage.Status.OK, res);
    }

    @ApiOperation(value = "이메일 인증 확인", notes = "insert email, token")
    @GetMapping("/check-email-token")
    public ApiMessage<EmailApiResponse> checkEmailToken(EmailConfirmApiRequest request){

        EmailApiResponse res = accountService.checkEmailConfirm(request.getEmail(), request.getToken());

        log.info("email 인증 완료");

        return ApiMessage.RESPONSE(ApiMessage.Status.OK, res);
    }

}