package org.rootservices.authorization.http.translator.exception;

/**
 * Created by tommackenzie on 12/13/14.
 */
public class ValidationError extends Exception {

    public ValidationError(String message) {
        super(message);
    }
}
