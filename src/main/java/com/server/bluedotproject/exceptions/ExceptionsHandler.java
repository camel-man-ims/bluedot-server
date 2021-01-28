package com.server.bluedotproject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

@ControllerAdvice
@ResponseBody
public class ExceptionsHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ErrorNotFoundException.class)
    public ErrorResponse handleNotFound(CustomException e){
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(e.getErrorCode().getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(NotExistException.class)
    public ErrorResponse userNotExist(CustomException e){
        return ErrorResponse.builder()
                .code(HttpStatus.CONFLICT)
                .message(e.getErrorCode().getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthorizationException.class)
    public ErrorResponse authorization(CustomException e){
        return ErrorResponse.builder()
                .code(HttpStatus.UNAUTHORIZED)
                .message(e.getErrorCode().getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InputException.class)
    public ErrorResponse inputError(CustomException e){
        return ErrorResponse.builder()
                .code(HttpStatus.UNAUTHORIZED)
                .message(e.getErrorCode().getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateException.class)
    public ErrorResponse duplicate(CustomException e){
        return ErrorResponse.builder()
                .code(HttpStatus.UNAUTHORIZED)
                .message(e.getErrorCode().getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(GenerateException.class)
    public ErrorResponse generate(CustomException e){
        return ErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(e.getErrorCode().getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .build();
    }


    /**
     * 로그인 세션 : 패스워드 틀렸을 때 예외처리
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PasswordWrongException.class)
    public ErrorResponse handlePasswordWrong(CustomException e){
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(e.getErrorCode().getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 로그인 세션 : 인증 실패
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AuthNNotAllowedException.class)
    public ErrorResponse handleAuthenticatedRequest(CustomException e){
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(e.getErrorCode().getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * JWT 토큰 : 인가 실패
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AuthRNotAllowedException.class)
    public ErrorResponse handleAuthorizedRequest(CustomException e){
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(e.getErrorCode().getMessage())
                .status(e.getErrorCode().getStatus())
                .timestamp(LocalDateTime.now())
                .build();

    }
}