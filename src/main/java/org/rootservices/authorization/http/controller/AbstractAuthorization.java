package org.rootservices.authorization.http.controller;

import org.glassfish.jersey.server.mvc.Viewable;
import org.rootservices.authorization.grant.ValidateParams;
import org.rootservices.authorization.grant.code.exception.InformClientException;
import org.rootservices.authorization.grant.code.exception.InformResourceOwnerException;
import org.rootservices.authorization.grant.code.factory.AuthRequestFactory;
import org.rootservices.authorization.grant.code.factory.exception.*;
import org.rootservices.authorization.grant.code.factory.optional.StateFactory;
import org.rootservices.authorization.grant.code.request.AuthRequest;
import org.rootservices.authorization.grant.code.request.ValidateAuthRequest;
import org.rootservices.authorization.http.factory.OkResponseFactory;
import org.rootservices.authorization.http.factory.ErrorResponseOrNotFound;
import org.rootservices.authorization.http.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by tommackenzie on 11/27/14.
 *
 * Scenario: Use a different template and response object for 200 status code.
 *
 * Given a resource owner, ro, a authorization server, auth server
 * When ro is redirected to /authorization?response_code=code&client_id=valid-client-id
 * And the request is validated
 * And the auth server does not want to render, authorization.mustache
 * And the auth server does not want to use OkResponse
 * Then extend AbstractAuthorization, A
 * And specify the response as the Generic Type.
 * And override getTemplateName() to return the name of the template
 * And implement a OkResponseBuilder, ORB, that builds the response desired.
 * And annotate, ORB with @Primary.
 *
 */
public abstract class AbstractAuthorization<OR> {

    @Autowired
    private ValidateParams validateParams;

    @Autowired
    private ErrorResponseOrNotFound errorResponseOrNotFound;

    @Autowired
    private OkResponseFactory okResponseFactory;

    public AbstractAuthorization() {}

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response authorize(@QueryParam("client_id") List<String> clientIds,
                              @QueryParam("response_type") List<String> responseTypes,
                              @QueryParam("state") List<String> states,
                              @QueryParam("scope") List<String> scopes,
                              @QueryParam("redirect_uri") List<String> redirectURIs) throws NotFoundException, URISyntaxException {

        try {
            validateParams.run(clientIds, responseTypes, redirectURIs, scopes, states);
        } catch (InformResourceOwnerException e) {
            throw new NotFoundException("Entity not found", e);
        } catch (InformClientException e) {
            return errorResponse(e.getError(), e.getRedirectURI());
        }

        String templateName = getTemplateName();
        OR okResponse = (OR) okResponseFactory.buildOkResponse();
        Viewable v = new Viewable(templateName, okResponse);
        return Response.ok().entity(v).build();
    }

    private Response errorResponse(String error, URI redirectURI) throws URISyntaxException {

        String formData = "#error="+error;
        URI location = new URI(redirectURI.toString() + formData);
        return Response.status(Response.Status.FOUND.getStatusCode())
                .location(location)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .build();
    }

    protected abstract String getTemplateName();
}
