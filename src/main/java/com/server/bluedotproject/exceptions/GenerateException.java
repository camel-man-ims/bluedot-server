package com.server.bluedotproject.exceptions;

public class GenerateException extends CustomException{

    public GenerateException(ErrorCode errorCode) {
        super(errorCode);
    }
}
