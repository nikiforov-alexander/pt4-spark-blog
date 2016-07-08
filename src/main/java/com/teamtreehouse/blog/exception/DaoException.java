package com.teamtreehouse.blog.exception;

public class DaoException extends Exception {
    private final Exception originalException;

    public DaoException(Exception originalException,
                        String message) {
        super(message);
        this.originalException = originalException;
        System.out.println(message);
    }
}
