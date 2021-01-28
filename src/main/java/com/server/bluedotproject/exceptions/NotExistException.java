package com.server.bluedotproject.exceptions;

public class NotExistException extends CustomException{
    public NotExistException(ErrorCode errorCode) {
        super(errorCode);
    }
}
