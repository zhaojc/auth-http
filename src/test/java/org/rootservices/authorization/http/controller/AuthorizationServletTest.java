package org.rootservices.authorization.http.controller;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Param;
import helpers.category.ServletContainer;
import helpers.fixture.FormFactory;
import helpers.fixture.MakeRandomEmailAddress;
import helpers.fixture.persistence.FactoryForPersistence;
import helpers.fixture.persistence.LoadConfidentialClientWithScopes;
import helpers.fixture.persistence.LoadResourceOwner;
import helpers.suite.IntegrationTestSuite;


import com.ning.http.client.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.authorization.http.GetServletURI;
import org.rootservices.authorization.http.GetServletURIImpl;
import org.rootservices.authorization.http.QueryStringToMap;
import org.rootservices.authorization.http.QueryStringToMapImpl;
import org.rootservices.authorization.persistence.entity.*;
import org.rootservices.authorization.security.RandomString;

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

    private static LoadConfidentialClientWithScopes loadConfidentialClientWithScopes;
    private static RandomString randomString;
    private static MakeRandomEmailAddress makeRandomEmailAddress;
    private static LoadResourceOwner loadResourceOwner;

    protected static Class ServletClass = AuthorizationServlet.class;
    protected static String baseURI = String.valueOf(IntegrationTestSuite.getServer().getURI());
    protected static String servletURI;

    @BeforeClass
    public static void beforeClass() {

        FactoryForPersistence factoryForPersistence = new FactoryForPersistence(
            IntegrationTestSuite.getContext()
        );

        loadConfidentialClientWithScopes = factoryForPersistence.makeLoadConfidentialClientWithScopes();

        // resource owner email address.
        randomString = IntegrationTestSuite.getContext().getBean(RandomString.class);
        makeRandomEmailAddress = new MakeRandomEmailAddress(randomString);

        loadResourceOwner = factoryForPersistence.makeLoadResourceOwner();

        GetServletURI getServletURI = new GetServletURIImpl();
        servletURI = getServletURI.run(baseURI, ServletClass);
    }

    @Test
    public void testGetExpect404() throws Exception {
        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testGetWithWrongResponseTypeExpect302() throws Exception {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=token";

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(302);
        String expectedLocaton = confidentialClient.getClient().getRedirectURI() + "?error=unauthorized_client";
        assertThat(response.getHeader("location")).isEqualTo(expectedLocaton);
    }

    @Test
    public void testGetExpect200() throws Exception {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testPostExpect403() throws URISyntaxException, ExecutionException, InterruptedException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();

        List<Param> postData = FormFactory.makeLoginForm("test@rootservices.org");

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(403);
    }

    @Test
    public void testPostExpect404() throws Exception {

        List<Param> postData = FormFactory.makeLoginForm("test@rootservices.org");

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testPostWithWrongResponseTypeExpect302() throws Exception {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();
        String expectedLocation = confidentialClient.getClient().getRedirectURI()
                + "?error=unauthorized_client";

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=token";

        List<Param> postData = FormFactory.makeLoginForm("test@rootservices.org");

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(response.getHeader("location"))
                .isEqualTo(expectedLocation);
    }

    @Test
    public void testPostExpectAuthCode() throws Exception {

        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        String email = makeRandomEmailAddress.run();
        ResourceOwner ro = loadResourceOwner.run(email);

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();

        List<Param> postData = FormFactory.makeLoginForm(email);

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(302);

        // location scheme, host, and path
        URI location = new URI(response.getHeader("location"));
        assertThat(location.getScheme()).isEqualTo(confidentialClient.getClient().getRedirectURI().getScheme());
        assertThat(location.getHost()).isEqualTo(confidentialClient.getClient().getRedirectURI().getHost());
        assertThat(location.getPath()).isEqualTo(confidentialClient.getClient().getRedirectURI().getPath());

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

        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        String email = makeRandomEmailAddress.run();
        ResourceOwner ro = loadResourceOwner.run(email);
        String state = "test-state";

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString() +
                "&state=" + state;

        List<Param> postData = FormFactory.makeLoginForm(email);

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(302);

        // location scheme, host, and path
        URI location = new URI(response.getHeader("location"));
        assertThat(location.getScheme()).isEqualTo(confidentialClient.getClient().getRedirectURI().getScheme());
        assertThat(location.getHost()).isEqualTo(confidentialClient.getClient().getRedirectURI().getHost());
        assertThat(location.getPath()).isEqualTo(confidentialClient.getClient().getRedirectURI().getPath());

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