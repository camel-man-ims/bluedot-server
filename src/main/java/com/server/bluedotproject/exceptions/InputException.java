package com.server.bluedotproject.exceptions;

public class InputException extends CustomException {
    public InputException(ErrorCode errorCode) {
        super(errorCode);
    }
}
