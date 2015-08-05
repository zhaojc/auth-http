package helpers.fixture.persistence;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;


import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tommackenzie on 8/3/15.
 */
public class GetSessionAndCsrfToken {
    private AsyncHttpClient httpDriver;
    private Pattern p;

    public GetSessionAndCsrfToken(AsyncHttpClient httpDriver) {
        this.httpDriver = httpDriver;
        p = Pattern.compile(".*\"csrfToken\" value=\"([^\"]*)\".*", Pattern.DOTALL);
    }

    public Session run(String uri) throws IOException, ExecutionException, InterruptedException {
        ListenableFuture<Response> f = httpDriver
                .prepareGet(uri)
                .execute();

        Response response = f.get();

        Session session = new Session();
        for(Cookie cookie: response.getCookies()) {
            if (cookie.getName().equals("JSESSIONID")) {
                session.setSession(cookie);
            }
        }

        session.setCsrfToken(extractCsrfToken(response.getResponseBody()).get());
        return session;
    }

    public Optional<String> extractCsrfToken(String responseBody) {
        Optional<String> csrfToken = Optional.empty();
        Matcher m = p.matcher(responseBody);
        if (m.matches()) {
            csrfToken = Optional.of(m.group(1));
        }
        return csrfToken;


    }
}
