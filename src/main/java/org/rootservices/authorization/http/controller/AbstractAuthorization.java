package org.rootservices.authorization.http.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;
import org.rootservices.authorization.codegrant.exception.client.InformClientException;
import org.rootservices.authorization.codegrant.exception.resourceowner.InformResourceOwnerException;
import org.rootservices.authorization.codegrant.params.ValidateParams;
import org.rootservices.authorization.codegrant.request.AuthRequest;
import org.rootservices.authorization.codegrant.request.ValidateAuthRequest;
import org.rootservices.authorization.codegrant.builder.AuthRequestBuilderImpl;
import org.rootservices.authorization.http.builder.OkResponseBuilder;
import org.rootservices.authorization.http.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
    private AuthRequestBuilderImpl authRequestBuilder;

    @Autowired
    private ValidateAuthRequest validateAuthRequest;

    @Autowired
    private OkResponseBuilder okResponseBuilder;

    public AbstractAuthorization() {}

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response authorize(@QueryParam("client_id") List<String> clientIds,
                              @QueryParam("response_type") List<String> responseTypes) throws NotFoundException {

        try {
            validateParams.run(clientIds, responseTypes);
            AuthRequest authRequest = authRequestBuilder.build(clientIds.get(0), responseTypes.get(0));
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
