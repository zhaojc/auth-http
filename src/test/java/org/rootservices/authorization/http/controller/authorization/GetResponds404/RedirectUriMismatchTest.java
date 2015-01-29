package org.rootservices.authorization.http.controller.authorization.GetResponds404;

import org.junit.Test;
import org.rootservices.authorization.persistence.entity.Client;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by tommackenzie on 1/28/15.
 */
public class RedirectUriMismatchTest extends NotFoundBase {

    @Test
    public void redirectUriIsEmpty() throws URISyntaxException {

        Client client = insert();

        Response response = target()
                .path("authorization")
                .queryParam("client_id", client.getUuid().toString())
                .queryParam("response_type", client.getResponseType())
                .queryParam("redirect_uri", "")
                .request()
                .get();

        assertEquals(NOT_FOUND, response.getStatus());

        String html = response.readEntity(String.class);
        String message = getMessage(html);
        assertThat(message).isEqualTo(NOT_FOUND_MESSAGE);
    }

    @Test
    public void redirectUriIsDuplicated() throws URISyntaxException {

        Client client = insert();

        Response response = target()
                .path("authorization")
                .queryParam("client_id", client.getUuid().toString())
                .queryParam("response_type", client.getResponseType())
                .queryParam("redirect_uri", client.getRedirectURI())
                .queryParam("redirect_uri", client.getRedirectURI())
                .request()
                .get();

        assertEquals(NOT_FOUND, response.getStatus());

        String html = response.readEntity(String.class);
        String message = getMessage(html);
        assertThat(message).isEqualTo(NOT_FOUND_MESSAGE);
    }

    @Test
    public void redirectUriNotFound() throws URISyntaxException {

        Client client = insert();
        URI mismatch_uri = new URI("https://rootservices.org/mismatch");

        Response response = target()
                .path("authorization")
                .queryParam("client_id", client.getUuid().toString())
                .queryParam("response_type", client.getResponseType())
                .queryParam("redirect_uri", mismatch_uri)
                .request()
                .get();

        assertEquals(NOT_FOUND, response.getStatus());

        String html = response.readEntity(String.class);
        String message = getMessage(html);
        assertThat(message).isEqualTo(NOT_FOUND_MESSAGE);
    }
}
