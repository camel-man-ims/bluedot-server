package com.server.bluedotproject.controller;

import com.server.bluedotproject.dto.request.CanvasApiRequest;
import com.server.bluedotproject.security.JwtTokenProvider;
import com.server.bluedotproject.service.CanvasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/canvas")
@RequiredArgsConstructor
public class CanvasController {

    private final CanvasService canvasService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("")
    public String create(@RequestBody CanvasApiRequest canvasApiRequest){
        return canvasService.create(canvasApiRequest);
    }
    public Long getUserIdFromHttpServletRequest(HttpServletRequest request){
        String token = request.getHeader("access_token");
        return jwtTokenProvider.getUserIdFromJwt(token);
    }
}
