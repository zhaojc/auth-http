package org.rootservices.authorization.http.response;

/**
 * Created by tommackenzie on 7/8/15.
 */
public class TokenError {
    private String error;
    private String description;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
