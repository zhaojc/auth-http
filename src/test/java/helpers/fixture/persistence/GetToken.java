package helpers.fixture.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.rootservices.authorization.grant.code.protocol.token.TokenResponse;
import org.rootservices.authorization.persistence.entity.ConfidentialClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

/**
 * Created by tommackenzie on 7/22/15.
 */
public class GetToken {
    private AsyncHttpClient httpDriver;
    private PostAuthorizationForm postAuthorizationForm;
    private ObjectMapper objectMapper;

    public GetToken(AsyncHttpClient httpDriver, ObjectMapper objectMapper) {
        this.httpDriver = httpDriver;
        this.objectMapper = objectMapper;
    }

    public TokenResponse run(ConfidentialClient confidentialClient, String authServletURI, String tokenURI, String authorizationCode) throws InterruptedException, ExecutionException, URISyntaxException, IOException {

        String payload = "{\"grant_type\": \"authorization_code\", " +
                "\"code\": \"" + authorizationCode + "\", " +
                "\"redirect_uri\": \"" + confidentialClient.getClient().getRedirectURI().toString() + "\"}";

        String credentials = confidentialClient.getClient().getUuid().toString() + ":password";

        String encodedCredentials = new String(
                Base64.getEncoder().encode(credentials.getBytes()),
                "UTF-8"
        );

        ListenableFuture<Response> f = httpDriver
                .preparePost(tokenURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + encodedCredentials)
                .setBody(payload)
                .execute();

        Response response = f.get();
        TokenResponse tokenResponse = objectMapper.readValue(response.getResponseBody(), TokenResponse.class);
        return tokenResponse;
    }
}
