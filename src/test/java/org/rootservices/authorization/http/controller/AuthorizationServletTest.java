package org.rootservices.authorization.http.controller;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Param;
import helpers.category.ServletContainer;
import helpers.suite.IntegrationTestSuite;


import com.ning.http.client.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.authorization.http.GetServletURI;
import org.rootservices.authorization.http.GetServletURIImpl;
import org.rootservices.authorization.http.QueryStringToMap;
import org.rootservices.authorization.http.QueryStringToMapImpl;
import org.rootservices.authorization.persistence.entity.Client;
import org.rootservices.authorization.persistence.entity.ResourceOwner;
import org.rootservices.authorization.persistence.entity.ResponseType;
import org.rootservices.authorization.persistence.repository.ClientRepository;
import org.rootservices.authorization.persistence.repository.ResourceOwnerRepository;
import org.rootservices.authorization.security.RandomString;
import org.rootservices.authorization.security.TextHasher;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by tommackenzie on 4/1/15.
 */
@Category(ServletContainer.class)
public class AuthorizationServletTest {

    private static ClientRepository clientRepository;
    protected static Class ServletClass = AuthorizationServlet.class;
    protected static String baseURI = String.valueOf(IntegrationTestSuite.getServer().getURI());
    protected static String servletURI;

    @BeforeClass
    public static void beforeClass() {
        clientRepository = IntegrationTestSuite.getContext().getBean(ClientRepository.class);
        GetServletURI getServletURI = new GetServletURIImpl();
        servletURI = getServletURI.run(baseURI, ServletClass);
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
    public void testGetExpect404() throws Exception {
        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testGetWithWrongResponseTypeExpect302() throws Exception {
        Client client = insertClient();

        String servletURI = this.servletURI +
                "?client_id=" + client.getUuid().toString() +
                "&response_type=token";

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(302);
        String expectedLocaton = client.getRedirectURI() + "?error=unauthorized_client";
        assertThat(response.getHeader("location")).isEqualTo(expectedLocaton);
    }

    @Test
    public void testGetExpect200() throws Exception {
        Client client = insertClient();

        String servletURI = this.servletURI +
                "?client_id=" + client.getUuid().toString() +
                "&response_type=" + client.getResponseType().toString();

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    private List<Param> loginFormData(String email) {
        Param userName = new Param("email", email);
        Param password = new Param("password", "password");
        List<Param> postData = new ArrayList<>();
        postData.add(userName);
        postData.add(password);

        return postData;
    }

    @Test
    public void testPostExpect403() throws URISyntaxException, ExecutionException, InterruptedException {
        Client client = insertClient();

        String servletURI = this.servletURI +
                "?client_id=" + client.getUuid().toString() +
                "&response_type=" + client.getResponseType().toString();

        List<Param> postData = loginFormData("test@rootservices.org");

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(403);
    }

    @Test
    public void testPostExpect404() throws Exception {

        List<Param> postData = loginFormData("test@rootservices.org");

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testPostWithWrongResponseTypeExpect302() throws Exception {
        Client client = insertClient();
        String expectedLocation = client.getRedirectURI()
                + "?error=unauthorized_client";

        String servletURI = this.servletURI +
                "?client_id=" + client.getUuid().toString() +
                "&response_type=token";

        List<Param> postData = loginFormData("test@rootservices.org");

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(response.getHeader("location"))
                .isEqualTo(expectedLocation);
    }

    /**
     * Avoids violation of unique key constraint in resource_owner schema.
     * resource_owner.email must be unique.
     *
     * @return
     */
    private String randomEmailAddress() {
        RandomString rs = (RandomString) IntegrationTestSuite.getContext().getBean(RandomString.class);
        String user = rs.run();
        return "auth-http-test-" + user + "@rootservices.org";
    }

    private ResourceOwner insertResourceOwner(String email) {
        ResourceOwner ro = new ResourceOwner();
        ro.setUuid(UUID.randomUUID());
        ro.setEmail(email);

        TextHasher textHasher = IntegrationTestSuite.getContext().getBean(TextHasher.class);

        String hashedPassword = textHasher.run("password");
        ro.setPassword(hashedPassword.getBytes());

        ResourceOwnerRepository resourceOwnerRepository = IntegrationTestSuite.getContext().getBean(ResourceOwnerRepository.class);
        resourceOwnerRepository.insert(ro);
        return ro;
    }

    @Test
    public void testPostExpectAuthCode() throws Exception {

        Client client = insertClient();

        String email = randomEmailAddress();
        ResourceOwner ro = insertResourceOwner(email);

        String servletURI = this.servletURI +
                "?client_id=" + client.getUuid().toString() +
                "&response_type=" + client.getResponseType().toString();

        List<Param> postData = loginFormData(email);

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(302);

        // location scheme, host, and path
        URI location = new URI(response.getHeader("location"));
        assertThat(location.getScheme()).isEqualTo(client.getRedirectURI().getScheme());
        assertThat(location.getHost()).isEqualTo(client.getRedirectURI().getHost());
        assertThat(location.getPath()).isEqualTo(client.getRedirectURI().getPath());

        //authorization code.
        QueryStringToMap queryStringToMap = new QueryStringToMapImpl();
        Map<String, List<String>> params = queryStringToMap.run(
                Optional.of(location.getQuery())
        );

        assertThat(params.size()).isEqualTo(1);
        assertThat(params.get("code").size()).isEqualTo(1);
        assertThat(params.get("code").get(0)).isNotNull();
    }

    @Test
    public void testPostWithStateExpectAuthCode() throws Exception {

        Client client = insertClient();

        String email = randomEmailAddress();
        ResourceOwner ro = insertResourceOwner(email);
        String state = "test-state";

        String servletURI = this.servletURI +
                "?client_id=" + client.getUuid().toString() +
                "&response_type=" + client.getResponseType().toString() +
                "&state=" + state;

        List<Param> postData = loginFormData(email);

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(302);

        // location scheme, host, and path
        URI location = new URI(response.getHeader("location"));
        assertThat(location.getScheme()).isEqualTo(client.getRedirectURI().getScheme());
        assertThat(location.getHost()).isEqualTo(client.getRedirectURI().getHost());
        assertThat(location.getPath()).isEqualTo(client.getRedirectURI().getPath());

        //authorization code.
        QueryStringToMap queryStringToMap = new QueryStringToMapImpl();
        Map<String, List<String>> params = queryStringToMap.run(
                Optional.of(location.getQuery())
        );

        assertThat(params.size()).isEqualTo(2);
        assertThat(params.get("code").size()).isEqualTo(1);
        assertThat(params.get("code").get(0)).isNotNull();
        assertThat(params.get("state").size()).isEqualTo(1);
        assertThat(params.get("state").get(0)).isEqualTo(state);
    }
}