package helpers.fixture.persistence;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Param;
import com.ning.http.client.Response;
import helpers.fixture.FormFactory;
import helpers.fixture.exception.GetCsrfException;
import org.rootservices.otter.QueryStringToMap;
import org.rootservices.otter.QueryStringToMapImpl;
import org.rootservices.authorization.persistence.entity.ConfidentialClient;
import org.rootservices.authorization.persistence.entity.ResourceOwner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Created by tommackenzie on 7/22/15.
 */
public class PostAuthorizationForm {
    private AsyncHttpClient httpDriver;
    private LoadResourceOwner loadResourceOwner;
    private GetSessionAndCsrfToken getSessionAndCsrfToken;

    public PostAuthorizationForm(AsyncHttpClient httpDriver, LoadResourceOwner loadResourceOwner, GetSessionAndCsrfToken getSessionAndCsrfToken) {
        this.httpDriver = httpDriver;
        this.loadResourceOwner = loadResourceOwner;
        this.getSessionAndCsrfToken = getSessionAndCsrfToken;
    }

    public String run(ConfidentialClient confidentialClient, String baseURI) throws IOException, ExecutionException, InterruptedException, URISyntaxException, GetCsrfException {
        ResourceOwner ro = loadResourceOwner.run();

        String servletURI = baseURI +
                "?client_id=" + confidentialClient.getClient().getUuid().toString() +
                "&response_type=" + confidentialClient.getClient().getResponseType().toString() +
                "&redirect_uri=" + URLEncoder.encode(confidentialClient.getClient().getRedirectURI().toString(), "UTF-8");

        Session session = getSessionAndCsrfToken.run(servletURI);
        List<Param> postData = FormFactory.makeLoginForm(ro.getEmail(), session.getCsrfToken());

        ListenableFuture<Response> f = httpDriver
                .preparePost(servletURI)
                .setFormParams(postData)
                .setCookies(Arrays.asList(session.getSession()))
                .execute();

        Response response = f.get();

        URI location = new URI(response.getHeader("location"));
        QueryStringToMap queryStringToMap = new QueryStringToMapImpl();
        Map<String, List<String>> params = queryStringToMap.run(
                Optional.of(location.getQuery())
        );

        return params.get("code").get(0);
    }
}
