package org.rootservices.authorization.http.factory;

import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.http.response.NotFoundResponse;

/**
 * Created by tommackenzie on 11/26/14.
 */
public interface NotFoundResponseFactory<NFR, NFE extends NotFoundException> {
    public NFR createNFR(NFE exception);
}
