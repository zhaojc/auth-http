package org.rootservices.authorization.http.exceptionmapper;

import org.glassfish.jersey.server.mvc.Viewable;
import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.http.factory.NotFoundResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Created by tommackenzie on 11/27/14.
 *
 * AbstractNotFoundExceptionMapper.
 *
 * Scenario: Use a different template and response object for 404 status code.
 *
 * Given a resource owner, ro, a authorization server, auth server
 * When ro is redirected to /authorization?response_code=blah&client_id=blah
 * And the auth server should inform the ro of validation errors
 * And the auth server does not want to render, /tempates/notFound.mustache
 * And the auth server does not want to use, NotFoundResponse
 * Then extend AbstractNotFoundExceptionMapper, NFEM, and specify the templateName
 * And implement a NotFoundResponse (Response Object)
 * And implement a NotFoundResponseFactory, NFRF
 * And annotate, NFEM, NFRF with @Primary.
 *
 */
public abstract class AbstractNotFoundExceptionMapper<NFR> implements ExceptionMapper<NotFoundException> {

    @Autowired
    private NotFoundResponseFactory notFoundResponseFactory;

    protected abstract String getTemplateName();

    @Override
    public Response toResponse(NotFoundException exception) {
        NFR response = (NFR) notFoundResponseFactory.createNFR(exception);
        String templateName = getTemplateName();
        Viewable v = new Viewable(templateName, response);

        return Response.status(Response.Status.NOT_FOUND).entity(v).type(MediaType.TEXT_HTML).build();
    }
}
