package com.server.bluedotproject.controller;

import com.server.bluedotproject.dto.request.TestRequest;
import com.server.bluedotproject.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

//    @Secured("ROLE_USER")
//    @PreAuthorize("hasRole('USER')")
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Secured("ROLE_MEMBER")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("")
    public String test(){
        return "+OK";
    }

    @GetMapping("/file")
    public Map fileTest(@RequestParam("file")MultipartFile file){

        Map<String,String> result = new HashMap<>();

        result.put("filename",file.getOriginalFilename());
        result.put("filesize", String.valueOf(file.getSize()));

        return result;
    }

    @PostMapping("/token")
    public void tokenTest(@Nullable HttpServletRequest request){

        System.out.println("하이");
    }

    @PostMapping("/user")
    public void UserCreateTest(@ModelAttribute TestRequest testRequest) throws URISyntaxException {
        System.out.println(testRequest.getFile());
        System.out.println(testRequest.getName());

    }
}
