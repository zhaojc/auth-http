package org.rootservices.authorization.http.controller.authorization.GetResponds200;

import org.junit.Test;
import org.rootservices.authorization.persistence.entity.Client;

import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Created by tommackenzie on 1/29/15.
 */
public class OptionalFieldsTest extends OkStatusCodeBase  {

    @Test
    public void authorizeValidRedirectURI() throws URISyntaxException {
        Client client = insert();

        Response response = target()
                .path("authorization")
                .queryParam("client_id", client.getUuid().toString())
                .queryParam("response_type", client.getResponseType())
                .queryParam("redirect_uri", client.getRedirectURI())
                .request()
                .get();

        assertEquals(OK, response.getStatus());
    }
}
