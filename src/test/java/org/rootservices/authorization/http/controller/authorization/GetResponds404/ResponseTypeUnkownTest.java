package org.rootservices.authorization.http.controller.authorization.GetResponds404;

import org.junit.Test;
import org.rootservices.authorization.persistence.entity.ResponseType;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by tommackenzie on 12/13/14.
 */
public class ResponseTypeUnkownTest extends ResponseTypeBase {
    @Test
    public void clientIdEmpty() throws URISyntaxException {

        Response response = target()
                .path("authorization")
                .queryParam("client_id", "")
                .queryParam("response_type", "unknown")
                .request()
                .get();

        assertEquals(NOT_FOUND, response.getStatus());

        String html = response.readEntity(String.class);
        String message = getMessage(html);
        assertThat(message).isEqualTo(NOT_FOUND_MESSAGE);
    }

    @Test
    public void clientNotFound() throws URISyntaxException, IOException {
        UUID uuid = UUID.randomUUID();

        Response response = target()
                .path("authorization")
                .queryParam("client_id", uuid.toString())
                .queryParam("response_type", "unknown")
                .request()
                .get();

        assertEquals(NOT_FOUND, response.getStatus());

        String html = response.readEntity(String.class);
        String message = getMessage(html);
        assertThat(message).isEqualTo(NOT_FOUND_MESSAGE);
    }

    @Test
    public void clientIdMissing() throws URISyntaxException {

        Response response = target()
                .path("authorization")
                .queryParam("response_type", "unknown")
                .request()
                .get();

        assertEquals(NOT_FOUND, response.getStatus());
        String html = response.readEntity(String.class);
        String message = getMessage(html);
        assertThat(message).isEqualTo(NOT_FOUND_MESSAGE);
    }

    @Test
    public void clientIdDuplicated() throws URISyntaxException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        Response response = target()
                .path("authorization")
                .queryParam("client_id", uuid1.toString())
                .queryParam("client_id", uuid2.toString())
                .queryParam("response_type", "unknown")
                .request()
                .get();

        assertEquals(NOT_FOUND, response.getStatus());

        String html = response.readEntity(String.class);
        String message = getMessage(html);
        assertThat(message).isEqualTo(NOT_FOUND_MESSAGE);
    }

    @Test
    public void clientIdIsNotUUID() throws URISyntaxException {
        String clientId = "invalidClientId";

        Response response = target()
                .path("authorization")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "unknown")
                .request()
                .get();

        assertEquals(NOT_FOUND, response.getStatus());
        String html = response.readEntity(String.class);
        String message = getMessage(html);
        assertThat(message).isEqualTo(NOT_FOUND_MESSAGE);
    }

}
