package org.rootservices.authorization.http.authentication;

import org.rootservices.authorization.http.authentication.exception.HttpBasicException;

/**
 * Created by tommackenzie on 6/4/15.
 */
public interface ParseHttpBasic {
    HttpBasicEntity run(String header) throws HttpBasicException;
}
