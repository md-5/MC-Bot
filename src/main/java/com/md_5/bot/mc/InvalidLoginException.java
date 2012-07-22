package com.md_5.bot.mc;

public class InvalidLoginException extends RuntimeException {

    public InvalidLoginException(String message) {
        super(message);
    }

    public InvalidLoginException(Throwable cause) {
        super(cause);
    }
}
