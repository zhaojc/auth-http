package org.rootservices.otter.authentication;

/**
 * Created by tommackenzie on 6/4/15.
 */
public class HttpBasicEntity {
    private String user;
    private String password;

    public HttpBasicEntity(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
