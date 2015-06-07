package org.rootservices.authorization.http.controller;

import com.google.gson.*;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Param;
import com.ning.http.client.Response;
import helpers.category.ServletContainer;
import helpers.fixture.FormFactory;
import helpers.fixture.MakeRandomEmailAddress;
import helpers.fixture.persistence.FactoryForPersistence;
import helpers.fixture.persistence.LoadConfidentialClientWithScopes;
import helpers.fixture.persistence.LoadResourceOwner;
import helpers.suite.IntegrationTestSuite;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.authorization.grant.code.protocol.token.TokenResponse;
import org.rootservices.authorization.http.GetServletURI;
import org.rootservices.authorization.http.GetServletURIImpl;
import org.rootservices.authorization.http.QueryStringToMap;
import org.rootservices.authorization.http.QueryStringToMapImpl;
import org.rootservices.authorization.persistence.entity.ConfidentialClient;
import org.rootservices.authorization.persistence.entity.ResourceOwner;
import org.rootservices.authorization.security.RandomString;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by tommackenzie on 6/5/15.
 */
@Category(ServletContainer.class)
public class TokenServletTest {

    private static LoadConfidentialClientWithScopes loadConfidentialClientWithScopes;
    private static RandomString randomString;
    private static MakeRandomEmailAddress makeRandomEmailAddress;
    private static LoadResourceOwner loadResourceOwner;

    protected static Class ServletClass = TokenServlet.class;
    protected static String baseURI = String.valueOf(IntegrationTestSuite.getServer().getURI());
    protected static GetServletURI getServletURI;
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

        getServletURI = new GetServletURIImpl();
        servletURI = getServletURI.run(baseURI, ServletClass);
    }

    // TODO: consider putting this in another place.
    public String postAuthorizationRequest(ConfidentialClient confidentialClient) throws ExecutionException, InterruptedException, UnsupportedEncodingException, URISyntaxException {

        String email = makeRandomEmailAddress.run();
        ResourceOwner ro = loadResourceOwner.run(email);

        String authorizationServletURI = getServletURI.run(baseURI, AuthorizationServlet.class);
        String servletURI = authorizationServletURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString();

        List<Param> postData = FormFactory.makeLoginForm(email);

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setFormParams(postData)
                .execute();

        Response response = f.get();

        URI location = new URI(response.getHeader("location"));
        QueryStringToMap queryStringToMap = new QueryStringToMapImpl();
        Map<String, List<String>> params = queryStringToMap.run(
                Optional.of(location.getQuery())
        );

        return params.get("code").get(0);
    }

    @Test
    public void testGetTokenExpect200() throws URISyntaxException, InterruptedException, ExecutionException, IOException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();
        String authorizationCode = postAuthorizationRequest(confidentialClient);

        // make the request get token.
        JsonObject payload = new JsonObject();
        payload.addProperty("grant_type", "authorization_code");
        payload.addProperty("code", authorizationCode);
        payload.addProperty("redirect_uri", confidentialClient.getClient().getRedirectURI().toString());

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

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");

        Gson jsonMarshal = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        TokenResponse tokenResponse = jsonMarshal.fromJson(response.getResponseBody(), TokenResponse.class);
        assertThat(tokenResponse.getTokenType()).isEqualTo("bearer");
        assertThat(tokenResponse.getExpiresIn()).isEqualTo(3600);
        assertThat(tokenResponse.getAccessToken()).isNotNull();
        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
    }

    @Test
    public void testGetTokenMissingAuthenticationHeaderExpect401() throws ExecutionException, InterruptedException {

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .preparePost(servletURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .execute();

        Response response = f.get();

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Basic");
    }

    @Test
    public void testGetTokenAuthCodeNotFoundExpect404() throws URISyntaxException, InterruptedException, ExecutionException, IOException {
        ConfidentialClient confidentialClient = loadConfidentialClientWithScopes.run();

        // make the request get token.
        JsonObject payload = new JsonObject();
        payload.addProperty("grant_type", "authorization_code");
        payload.addProperty("code", "invalid-authorization-code");
        payload.addProperty("redirect_uri", confidentialClient.getClient().getRedirectURI().toString());

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

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getResponseBody()).isEmpty();
    }
}