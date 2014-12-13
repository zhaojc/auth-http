package org.rootservices.authorization.http.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;
import org.rootservices.authorization.codegrant.exception.client.InformClientException;
import org.rootservices.authorization.codegrant.exception.resourceowner.InformResourceOwnerException;
import org.rootservices.authorization.codegrant.request.AuthRequest;
import org.rootservices.authorization.codegrant.request.ValidateAuthRequest;
import org.rootservices.authorization.http.builder.OkResponseBuilder;
import org.rootservices.authorization.http.context.RedirectOrNotFound;
import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.http.translator.StringsToResponseType;
import org.rootservices.authorization.http.translator.StringsToUUID;
import org.rootservices.authorization.http.translator.ValidationError;
import org.rootservices.authorization.persistence.entity.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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
    private StringsToUUID stringsToUUID;

    @Autowired
    private StringsToResponseType stringsToResponseType;

    @Autowired
    private ValidateAuthRequest validateAuthRequest;

    @Autowired
    private RedirectOrNotFound redirectOrNotFound;

    @Autowired
    private OkResponseBuilder okResponseBuilder;

    public AbstractAuthorization() {}

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response authorize(@QueryParam("client_id") List<String> clientIds,
                              @QueryParam("response_type") List<String> responseTypes) throws NotFoundException {

        UUID clientId;
        ResponseType responseType;

        try {
            clientId = stringsToUUID.run(clientIds);
        } catch (ValidationError e) {
            throw new NotFoundException("Entity not found", e);
        }

        try {
            responseType = stringsToResponseType.run(responseTypes);
        } catch (ValidationError e) {
            return redirectOrNotFound.run(clientId);
        }

        AuthRequest authRequest = new AuthRequest();
        authRequest.setClientId(clientId);
        authRequest.setResponseType(responseType);

        try {
            validateAuthRequest.run(authRequest);
        } catch (InformResourceOwnerException e) {
            throw new NotFoundException("Entity not found", e);
        } catch (InformClientException e) {
            e.printStackTrace();
        }

        String templateName = getTemplateName();
        OR okResponse = (OR) okResponseBuilder.buildOkResponse();
        Viewable v = new Viewable(templateName, okResponse);
        return Response.ok().entity(v).build();
    }

    protected abstract String getTemplateName();
}
