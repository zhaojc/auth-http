package org.rootservices.authorization.http.exception;

/**
 * Created by tommackenzie on 11/23/14.
 */
public class NotFoundException extends Exception {

    private Throwable throwable;

    public NotFoundException(String message, Throwable throwable) {
        super(message);
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
