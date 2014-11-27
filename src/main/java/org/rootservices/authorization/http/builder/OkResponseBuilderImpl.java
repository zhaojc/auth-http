package org.rootservices.authorization.http.builder;

import org.rootservices.authorization.http.response.OkResponse;
import org.springframework.stereotype.Component;

/**
 * Created by tommackenzie on 11/27/14.
 */
@Component
public class OkResponseBuilderImpl implements OkResponseBuilder<OkResponse> {

    @Override
    public OkResponse buildOkResponse() {
        return new OkResponse();
    }
}
