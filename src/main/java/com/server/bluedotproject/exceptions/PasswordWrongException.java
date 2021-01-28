package com.server.bluedotproject.exceptions;

public class PasswordWrongException extends CustomException {
    public PasswordWrongException(ErrorCode errorCode) {
        super(errorCode);
    }

}
