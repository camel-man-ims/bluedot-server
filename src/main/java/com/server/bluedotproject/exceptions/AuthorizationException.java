package com.server.bluedotproject.exceptions;

public class AuthorizationException extends CustomException{
    public AuthorizationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
