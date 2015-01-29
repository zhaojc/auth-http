package org.rootservices.authorization.http.controller.authorization.GetResponds200;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.rootservices.authorization.persistence.entity.Client;
import org.rootservices.authorization.persistence.entity.ResponseType;
import org.rootservices.authorization.persistence.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by tommackenzie on 11/27/14.
 */
public class RequiredFieldsOnlyTest extends OkStatusCodeBase {

    @Test
    public void authorize() throws URISyntaxException {
        Client client = insert();

        Response response = target()
                .path("authorization")
                .queryParam("client_id", client.getUuid().toString())
                .queryParam("response_type", client.getResponseType())
                .request()
                .get();

        assertEquals(OK, response.getStatus());
    }
}
