package org.rootservices.authorization.http.controller;

import org.glassfish.jersey.server.mvc.Viewable;
import org.rootservices.authorization.codegrant.exception.client.ResponseTypeIsNotCodeException;
import org.rootservices.authorization.codegrant.exception.client.UnAuthorizedResponseTypeException;
import org.rootservices.authorization.codegrant.exception.resourceowner.ClientNotFoundException;
import org.rootservices.authorization.codegrant.request.AuthRequest;
import org.rootservices.authorization.codegrant.request.ValidateAuthRequest;
import org.rootservices.authorization.http.builder.OkResponseBuilder;
import org.rootservices.authorization.http.context.InvalidRequestOrNotFound;
import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.http.translator.StringsToResponseType;
import org.rootservices.authorization.http.translator.StringsToState;
import org.rootservices.authorization.http.translator.StringsToUUID;
import org.rootservices.authorization.http.translator.ValidationError;
import org.rootservices.authorization.persistence.entity.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
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
    private StringsToState stringsToState;

    @Autowired
    private ValidateAuthRequest validateAuthRequest;

    @Autowired
    private InvalidRequestOrNotFound invalidRequestOrNotFound;

    @Autowired
    private OkResponseBuilder okResponseBuilder;

    public AbstractAuthorization() {}

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response authorize(@QueryParam("client_id") List<String> clientIds,
                              @QueryParam("response_type") List<String> responseTypes,
                              @QueryParam("state") List<String> states) throws NotFoundException, URISyntaxException {

        UUID clientId;
        ResponseType responseType;
        String state;


        try {
            clientId = stringsToUUID.run(clientIds);
        } catch (ValidationError e) {
            throw new NotFoundException("Entity not found", e);
        }

        try {
            responseType = stringsToResponseType.run(responseTypes);
        } catch (ValidationError e) {
            return invalidRequestOrNotFound.run(clientId);
        }

        try {
            state = stringsToState.run(states);
        } catch (ValidationError validationError) {
            return invalidRequestOrNotFound.run(clientId);
        }

        AuthRequest authRequest = new AuthRequest();
        authRequest.setClientId(clientId);
        authRequest.setResponseType(responseType);

        try {
            validateAuthRequest.run(authRequest);
        } catch (ClientNotFoundException e) {
            throw new NotFoundException("Entity not found", e);
        } catch (UnAuthorizedResponseTypeException e) {
            String formData = "#error=unauthorized_client";
            URI location = new URI(e.getRedirectURI().toString() + formData);
            Response response = Response.status(Response.Status.FOUND.getStatusCode())
                    .location(location)
                    .type(MediaType.APPLICATION_FORM_URLENCODED)
                    .build();

            return response;

        } catch (ResponseTypeIsNotCodeException e) {
            e.printStackTrace();
        }

        String templateName = getTemplateName();
        OR okResponse = (OR) okResponseBuilder.buildOkResponse();
        Viewable v = new Viewable(templateName, okResponse);
        return Response.ok().entity(v).build();
    }

    protected abstract String getTemplateName();
}
