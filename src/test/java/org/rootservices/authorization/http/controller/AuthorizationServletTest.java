package org.rootservices.authorization.http.controller;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Param;
import helpers.category.ServletContainer;
import helpers.fixture.FormFactory;
import helpers.fixture.persistence.*;
import helpers.suite.IntegrationTestSuite;


import com.ning.http.client.Response;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.otter.router.GetServletURI;
import org.rootservices.otter.router.GetServletURIImpl;
import org.rootservices.otter.QueryStringToMap;
import org.rootservices.otter.QueryStringToMapImpl;
import org.rootservices.authorization.persistence.entity.*;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by tommackenzie on 4/1/15.
 */
@Category(ServletContainer.class)
public class AuthorizationServletTest {

    private static LoadConfidentialClientWithScopes loadConfidentialClientWithScopes;
    private static LoadResourceOwner loadResourceOwner;
    private static GetSessionAndCsrfToken getSessionAndCsrfToken;

    protected static Class ServletClass = AuthorizationServlet.class;
    protected static String baseURI = String.valueOf(IntegrationTestSuite.getServer().getURI());
    protected static String servletURI;

    @BeforeClass
    public static void beforeClass() {

        FactoryForPersistence factoryForPersistence = new FactoryForPersistence(
            IntegrationTestSuite.getContext()
        );

        loadConfidentialClientWithScopes = factoryForPersistence.makeLoadConfidentialClientWithScopes();
        loadResourceOwner = factoryForPersistence.makeLoadResourceOwner();
        getSessionAndCsrfToken = factoryForPersistence.makeGetSessionAndCsrfToken();

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

        Optional<String> csrfToken = getSessionAndCsrfToken.extractCsrfToken(response.getResponseBody());
        assertTrue(csrfToken.isPresent());
    }

    @Test
    public void testGetWithRedirectUriExpect200() throws Exception {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString() +
                "&redirect_uri=" + URLEncoder.encode(confidentialClient.getClient().getRedirectURI().toString(), "UTF-8");

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient().prepareGet(servletURI).execute();
        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testPostWhenNoSessionAndWrongCsrfTokenExpectCsrfFailureAnd403() throws URISyntaxException, ExecutionException, InterruptedException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();
        ResourceOwner ro = loadResourceOwner.run();

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();

        List<Param> postData = FormFactory.makeLoginForm(ro.getEmail(), "foo");

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(403);
    }

    @Test
    public void testPostWhenWrongCsrfTokenExpectCsrfFailureAnd403() throws URISyntaxException, ExecutionException, InterruptedException, IOException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();
        ResourceOwner ro = loadResourceOwner.run();

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();

        Session session = getSessionAndCsrfToken.run(servletURI);
        List<Param> postData = FormFactory.makeLoginForm(ro.getEmail(), "foo");

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .setCookies(Arrays.asList(session.getSession()))
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(403);
    }


    @Test
    public void testPostWhenResourceOwnerFailsAuthenticationExpect403() throws URISyntaxException, ExecutionException, InterruptedException, IOException {

        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();

        Session session = getSessionAndCsrfToken.run(servletURI);
        List<Param> postData = FormFactory.makeLoginForm("unknown-user@rootservices.org", session.getCsrfToken());

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .setCookies(Arrays.asList(session.getSession()))
                .execute();

        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(403);
    }

    @Test
    public void testPostWithMissingParamsExpect404() throws Exception {

        // get a session and valid csrf.
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();
        String serletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();
        Session session = getSessionAndCsrfToken.run(serletURI);

        ResourceOwner ro = loadResourceOwner.run();
        List<Param> postData = FormFactory.makeLoginForm(ro.getEmail(), session.getCsrfToken());


        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .setCookies(Arrays.asList(session.getSession()))
                .execute();

        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testPostWithWrongResponseTypeExpect302() throws Exception {

        // get a session and valid csrf.
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        String validServletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();
        Session session = getSessionAndCsrfToken.run(validServletURI);

        ResourceOwner ro = loadResourceOwner.run();
        List<Param> postData = FormFactory.makeLoginForm(ro.getEmail(), session.getCsrfToken());

        String expectedLocation = confidentialClient.getClient().getRedirectURI()
                + "?error=unauthorized_client";

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=token";

        // make request with wrong response type.
        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .setCookies(Arrays.asList(session.getSession()))
                .execute();

        Response response = f.get();
        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(response.getHeader("location"))
                .isEqualTo(expectedLocation);
    }

    @Test
    public void testPostExpectAuthCode() throws Exception {

        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        ResourceOwner ro = loadResourceOwner.run();

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();

        Session session = getSessionAndCsrfToken.run(servletURI);
        List<Param> postData = FormFactory.makeLoginForm(ro.getEmail(), session.getCsrfToken());

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .setCookies(Arrays.asList(session.getSession()))
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
        String state = "test-state";

        String servletURI = this.servletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString() +
                "&state=" + state;

        Session session = getSessionAndCsrfToken.run(servletURI);
        ResourceOwner ro = loadResourceOwner.run();
        List<Param> postData = FormFactory.makeLoginForm(ro.getEmail(), session.getCsrfToken());

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .setCookies(Arrays.asList(session.getSession()))
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