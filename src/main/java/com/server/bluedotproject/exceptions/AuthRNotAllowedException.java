package com.server.bluedotproject.exceptions;

public class AuthRNotAllowedException extends CustomException {
    public AuthRNotAllowedException(ErrorCode errorCode) {
        super(errorCode);
    }

}