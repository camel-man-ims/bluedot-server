package com.server.bluedotproject.exceptions;

public class AuthNNotAllowedException extends CustomException {
    public AuthNNotAllowedException(ErrorCode errorCode) {
        super(errorCode);
    }

}