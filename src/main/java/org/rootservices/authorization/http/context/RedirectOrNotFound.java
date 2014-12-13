package org.rootservices.authorization.http.context;

import org.rootservices.authorization.context.GetClientRedirectURI;
import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.persistence.exceptions.RecordNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.UUID;

/**
 * Created by tommackenzie on 12/10/14.
 */
@Component
public class RedirectOrNotFound {

    @Autowired
    private GetClientRedirectURI getClientRedirectURI;

    public Response run(UUID clientId) throws NotFoundException {
        URI redirectURI;

        try {
            redirectURI = getClientRedirectURI.run(clientId);
        } catch (RecordNotFoundException rnfe) {
            throw new NotFoundException("Entity not found", rnfe);
        }

        return Response.status(Status.MOVED_PERMANENTLY.getStatusCode())
                .location(redirectURI)
                .build();
    }
}
