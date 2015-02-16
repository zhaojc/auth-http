package org.rootservices.authorization.http.factory;

import org.rootservices.authorization.http.response.OkResponse;
import org.springframework.stereotype.Component;

/**
 * Created by tommackenzie on 11/27/14.
 */
@Component
public class OkResponseFactoryImpl implements OkResponseFactory<OkResponse> {

    @Override
    public OkResponse buildOkResponse() {
        return new OkResponse();
    }
}
