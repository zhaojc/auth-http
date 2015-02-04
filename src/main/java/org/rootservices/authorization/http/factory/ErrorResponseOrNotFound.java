package org.rootservices.authorization.http.factory;

import org.rootservices.authorization.context.GetClientRedirectURI;
import org.rootservices.authorization.context.GetClientRedirectURIImpl;
import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.persistence.exceptions.RecordNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Created by tommackenzie on 12/10/14.
 */
@Component
public class ErrorResponseOrNotFound {

    @Autowired
    private GetClientRedirectURI getClientRedirectURI;
    private String error = "invalid_request";

    public Response run(UUID clientId) throws NotFoundException, URISyntaxException {
        return run(clientId, error);
    }

    public Response run(UUID clientId, String error) throws NotFoundException, URISyntaxException {
        URI redirectURI;

        try {
            redirectURI = getClientRedirectURI.run(clientId);
        } catch (RecordNotFoundException rnfe) {
            throw new NotFoundException("Entity not found", rnfe);
        }

        String formData = "#error="+error;
        URI location = new URI(redirectURI.toString() + formData);
        return Response.status(Status.FOUND.getStatusCode())
                .location(location)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .build();
    }
}
