package org.rootservices.authorization.http.controller.authorization.GetResponds404;

import org.glassfish.jersey.test.JerseyTest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.runner.RunWith;
import org.rootservices.authorization.persistence.entity.Client;
import org.rootservices.authorization.persistence.entity.ResponseType;
import org.rootservices.authorization.persistence.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Created by tommackenzie on 12/10/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value={"classpath:applicationContext.xml"})
public class ResponseTypeBase extends JerseyTest {

    protected static int NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();
    protected static String NOT_FOUND_MESSAGE = "Not Found";

    @Autowired
    private ClientRepository clientRepository;

    @Override
    protected Application configure() {
        return new org.rootservices.server.Application();
    }

    public Client insert() throws URISyntaxException {
        UUID uuid = UUID.randomUUID();
        ResponseType rt = ResponseType.CODE;
        URI redirectURI = new URI("https://rootservices.org");
        Client client = new Client(uuid, rt, redirectURI);

        clientRepository.insert(client);
        return client;
    }

    public String getMessage(String html) {
        Document doc = Jsoup.parse(html);
        return doc.select("div#message").first().text();
    }
}
