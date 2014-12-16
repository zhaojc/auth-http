package org.rootservices.authorization.http.controller.authorization;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rootservices.authorization.persistence.entity.Client;
import org.rootservices.authorization.persistence.entity.ResponseType;
import org.rootservices.authorization.persistence.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by tommackenzie on 12/13/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value={"classpath:applicationContext.xml"})
public class GetResponds302Test extends JerseyTest {

    private static int FOUND = Response.Status.FOUND.getStatusCode();

    @Autowired
    private ClientRepository clientRepository;

    @Override
    protected Application configure() {
        return new org.rootservices.server.Application();
    }

    public Client insert(ResponseType rt) throws URISyntaxException {
        UUID uuid = UUID.randomUUID();
        URI redirectURI = new URI("https://rootservices.org");
        Client client = new Client(uuid, rt, redirectURI);

        clientRepository.insert(client);
        return client;
    }

    @Test
    public void UnAuthorizedResponseType() throws URISyntaxException {
        Client client = insert(ResponseType.TOKEN);
        URI expectedLocation = new URI(client.getRedirectURI() + "?error=unauthorized_client");

        Response response = target()
                .path("authorization")
                .queryParam("client_id", client.getUuid().toString())
                .queryParam("response_type", ResponseType.CODE.toString())
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(FOUND);
        assertThat(response.getLocation()).isEqualTo(expectedLocation);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }
}
