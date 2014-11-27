package org.rootservices.authorization.http.factory;

import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.http.response.NotFoundResponse;
import org.springframework.stereotype.Component;

/**
 * Created by tommackenzie on 11/26/14.
 */
@Component
public class NotFoundResponseFactoryImpl implements NotFoundResponseFactory<NotFoundResponse, NotFoundException> {

    @Override
    public NotFoundResponse createNFR(NotFoundException exception) {
        NotFoundResponse notFoundResponse = new NotFoundResponse();
        notFoundResponse.message = "Not Found";

        return notFoundResponse;
    }
}
