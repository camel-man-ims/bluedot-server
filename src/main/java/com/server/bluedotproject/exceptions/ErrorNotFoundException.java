package com.server.bluedotproject.exceptions;

public class ErrorNotFoundException extends CustomException{

    public ErrorNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}

