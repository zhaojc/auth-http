package org.rootservices.authorization.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import helpers.category.ServletContainer;
import helpers.fixture.exception.GetCsrfException;
import helpers.fixture.persistence.*;
import helpers.suite.IntegrationTestSuite;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.authorization.grant.code.protocol.token.TokenResponse;
import org.rootservices.otter.router.GetServletURI;
import org.rootservices.otter.router.GetServletURIImpl;
import org.rootservices.authorization.http.response.Error;
import org.rootservices.authorization.persistence.entity.ConfidentialClient;
import org.rootservices.config.AppConfig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by tommackenzie on 6/5/15.
 */
@Category(ServletContainer.class)
public class TokenServletTest {

    private static LoadConfidentialClientWithScopes loadConfidentialClientWithScopes;
    private static PostAuthorizationForm postAuthorizationForm;
    private static GetToken getToken;
    protected static Class ServletClass = TokenServlet.class;
    protected static String baseURI = String.valueOf(IntegrationTestSuite.getServer().getURI());
    protected static GetServletURI getServletURI;
    protected static String servletURI;
    protected static String authServletURI;

    @BeforeClass
    public static void beforeClass() {

        FactoryForPersistence factoryForPersistence = new FactoryForPersistence(
                IntegrationTestSuite.getContext()
        );

        loadConfidentialClientWithScopes = factoryForPersistence.makeLoadConfidentialClientWithScopes();
        getServletURI = new GetServletURIImpl();
        servletURI = getServletURI.run(baseURI, ServletClass);
        authServletURI = getServletURI.run(baseURI, AuthorizationServlet.class);
        postAuthorizationForm = factoryForPersistence.makePostAuthorizationForm();
        getToken = factoryForPersistence.makeGetToken();
    }

    @Test
    public void testGetTokenExpect200() throws URISyntaxException, InterruptedException, ExecutionException, IOException, GetCsrfException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();
        String authorizationCode = postAuthorizationForm.run(confidentialClient, authServletURI);

        String payload = "{\"grant_type\": \"authorization_code\", " +
                "\"code\": \"" + authorizationCode + "\", " +
                "\"redirect_uri\": \"" + confidentialClient.getClient().getRedirectURI().toString() + "\"}";

        String credentials = confidentialClient.getClient().getUuid().toString() + ":password";

        String encodedCredentials = new String(
                Base64.getEncoder().encode(credentials.getBytes()),
                "UTF-8"
        );

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + encodedCredentials)
                .setBody(payload)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");

        AppConfig config = new AppConfig();
        ObjectMapper om = config.objectMapper();

        TokenResponse tokenResponse = om.readValue(response.getResponseBody(), TokenResponse.class);
        assertThat(tokenResponse.getTokenType()).isEqualTo("bearer");
        assertThat(tokenResponse.getExpiresIn()).isEqualTo(3600);
        assertThat(tokenResponse.getAccessToken()).isNotNull();
        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
    }

    @Test
    public void testGetTokenCodeIsMissingExpect400() throws URISyntaxException, InterruptedException, ExecutionException, IOException, GetCsrfException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();
        String authorizationCode = postAuthorizationForm.run(confidentialClient, authServletURI);

        String payload = "{\"grant_type\": \"authorization_code\", " +
                "\"redirect_uri\": \"" + confidentialClient.getClient().getRedirectURI().toString() + "\"}";

        String credentials = confidentialClient.getClient().getUuid().toString() + ":password";

        String encodedCredentials = new String(
                Base64.getEncoder().encode(credentials.getBytes()),
                "UTF-8"
        );

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + encodedCredentials)
                .setBody(payload)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");

        AppConfig config = new AppConfig();
        ObjectMapper om = config.objectMapper();

        Error error = om.readValue(response.getResponseBody(), Error.class);
        assertThat(error.getError()).isEqualTo("invalid_request");
        assertThat(error.getDescription()).isEqualTo("code is a required field");
    }

    @Test
    public void testGetTokenRedirectUriIsMissingExpect400() throws URISyntaxException, InterruptedException, ExecutionException, IOException, GetCsrfException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();
        String authorizationCode = postAuthorizationForm.run(confidentialClient, authServletURI);

        String payload = "{\"grant_type\": \"authorization_code\", " +
                "\"code\": \"" + authorizationCode + "\"}";

        String credentials = confidentialClient.getClient().getUuid().toString() + ":password";

        String encodedCredentials = new String(
                Base64.getEncoder().encode(credentials.getBytes()),
                "UTF-8"
        );

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + encodedCredentials)
                .setBody(payload)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");

        AppConfig config = new AppConfig();
        ObjectMapper om = config.objectMapper();

        Error error = om.readValue(response.getResponseBody(), Error.class);
        assertThat(error.getError()).isEqualTo("invalid_grant");
        assertThat(error.getDescription()).isEqualTo(null);
    }

    @Test
    public void testGetTokenMissingAuthenticationHeaderExpect401() throws ExecutionException, InterruptedException, IOException {

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Basic");

        AppConfig config = new AppConfig();
        ObjectMapper om = config.objectMapper();

        Error error = om.readValue(response.getResponseBody(), Error.class);
        assertThat(error.getError()).isEqualTo("invalid_client");
        assertThat(error.getDescription()).isEqualTo(null);

    }

    @Test
    public void testGetTokenAuthenticationFailsWrongPasswordExpect401() throws ExecutionException, InterruptedException, IOException, URISyntaxException, GetCsrfException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();
        String authorizationCode = postAuthorizationForm.run(confidentialClient, authServletURI);

        String payload = "{\"grant_type\": \"authorization_code\", " +
                "\"code\": \"" + authorizationCode + "\", " +
                "\"redirect_uri\": \"" + confidentialClient.getClient().getRedirectURI().toString() + "\"}";

        String credentials = confidentialClient.getClient().getUuid().toString() + ":wrong-password";

        String encodedCredentials = new String(
                Base64.getEncoder().encode(credentials.getBytes()),
                "UTF-8"
        );

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + encodedCredentials)
                .setBody(payload)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Basic");

        AppConfig config = new AppConfig();
        ObjectMapper om = config.objectMapper();

        Error error = om.readValue(response.getResponseBody(), Error.class);
        assertThat(error.getError()).isEqualTo("invalid_client");
        assertThat(error.getDescription()).isEqualTo(null);
    }

    @Test
    public void testGetTokenAuthCodeNotFoundExpect400() throws URISyntaxException, InterruptedException, ExecutionException, IOException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        String payload = "{\"grant_type\": \"authorization_code\", " +
                "\"code\": \"invalid-authorization-code\", " +
                "\"redirect_uri\": \"" + confidentialClient.getClient().getRedirectURI().toString() + "\"}";

        String credentials = confidentialClient.getClient().getUuid().toString() + ":password";

        String encodedCredentials = new String(
                Base64.getEncoder().encode(credentials.getBytes()),
                "UTF-8"
        );

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + encodedCredentials)
                .setBody(payload.toString())
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(400);

        AppConfig config = new AppConfig();
        ObjectMapper om = config.objectMapper();

        Error error = om.readValue(response.getResponseBody(), Error.class);
        assertThat(error.getError()).isEqualTo("invalid_grant");
        assertThat(error.getDescription()).isEqualTo(null);
    }

    @Test
    public void testGetTokenCodeIsCompromisedExpect400() throws URISyntaxException, InterruptedException, ExecutionException, IOException, GetCsrfException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        // generate a token with a auth code.
        String authorizationCode = postAuthorizationForm.run(confidentialClient, authServletURI);
        TokenResponse tokenResponse = getToken.run(confidentialClient, authServletURI, servletURI, authorizationCode);

        // attempt to use the auth code a second time.
        String payload = "{\"grant_type\": \"authorization_code\", " +
                "\"code\": \"" + authorizationCode + "\", " +
                "\"redirect_uri\": \"" + confidentialClient.getClient().getRedirectURI().toString() + "\"}";

        String credentials = confidentialClient.getClient().getUuid().toString() + ":password";

        String encodedCredentials = new String(
                Base64.getEncoder().encode(credentials.getBytes()),
                "UTF-8"
        );

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + encodedCredentials)
                .setBody(payload)
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");

        AppConfig config = new AppConfig();
        ObjectMapper om = config.objectMapper();

        Error error = om.readValue(response.getResponseBody(), Error.class);
        assertThat(error.getError()).isEqualTo("invalid_grant");
        assertThat(error.getDescription()).isEqualTo(null);
    }
}
