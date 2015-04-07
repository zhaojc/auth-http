package org.rootservices.authorization.http.controller;

import com.ning.http.client.ListenableFuture;
import helpers.category.ServletContainer;
import helpers.suite.IntegrationTestSuite;


import com.ning.http.client.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.authorization.persistence.entity.Client;
import org.rootservices.authorization.persistence.entity.ResponseType;
import org.rootservices.authorization.persistence.repository.ClientRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.annotation.WebServlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by tommackenzie on 4/1/15.
 */
@Category(ServletContainer.class)
public class AuthorizationServletTest {

    private static ClientRepository clientRepository;
    private static String servletURI;

    @BeforeClass
    public static void beforeClass() {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        clientRepository = context.getBean(ClientRepository.class);
        servletURI = calculateServletURI();
    }

    /**
     * Dynamically determines the uri to the servlet in test.
     *
     * @return
     */
    public static String calculateServletURI() {
        String baseURI = String.valueOf(IntegrationTestSuite.getServer().getURI());
        WebServlet webServlet = AuthorizationServlet.class.getAnnotation(WebServlet.class);

        // prevent duplicate "/"
        if (baseURI.endsWith("/") && webServlet.value()[0].startsWith("/")) {
            baseURI = baseURI.substring(0, baseURI.length()-1);
        }
        return baseURI + webServlet.value()[0];
    }

    public Client insertClient() throws URISyntaxException {

        UUID uuid = UUID.randomUUID();
        ResponseType rt = ResponseType.CODE;
        URI redirectUri = new URI("https://rootservices.org");
        Client client = new Client(uuid, rt, redirectUri);

        clientRepository.insert(client);
        return client;
    }

    @Test
    public void testDoGetExpect404() throws Exception {
        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testDoGetWithWrongResponseTypeExpect302() throws Exception {
        Client client = insertClient();

        String servletURI = this.servletURI +
                "?client_id=" + client.getUuid().toString() +
                "&response_type=token";

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(response.getHeader("location")).isEqualTo(client.getRedirectURI().toString());
    }

    @Test
    public void testDoGetExpect200() throws Exception {
        Client client = insertClient();

        String servletURI = this.servletURI +
                "?client_id=" + client.getUuid().toString() +
                "&response_type=" + client.getResponseType().toString();

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
}