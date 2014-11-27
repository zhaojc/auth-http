package org.rootservices.authorization.http.exceptionmapper;

import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.http.response.NotFoundResponse;

import javax.ws.rs.ext.Provider;

/**
 * Created by tommackenzie on 11/27/14.
 */
@Provider
public class NotFoundExceptionMapper extends AbstractNotFoundExceptionMapper<NotFoundResponse> {

    @Override
    protected String getTemplateName() {
        return "/notFound.mustache";
    }

}
