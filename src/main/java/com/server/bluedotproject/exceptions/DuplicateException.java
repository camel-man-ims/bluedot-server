package com.server.bluedotproject.exceptions;

public class DuplicateException extends CustomException{
    public DuplicateException(ErrorCode errorCode) {
        super(errorCode);
    }
}
