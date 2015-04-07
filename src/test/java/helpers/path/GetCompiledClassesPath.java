package helpers.path;

import org.rootservices.authorization.http.controller.AuthorizationServlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.ProtectionDomain;

/**
 * Created by tommackenzie on 4/1/15.
 */
public class GetCompiledClassesPath {

    /**
     * Gets the uri to the project's target/classes location.
     *
     * @return
     * @throws URISyntaxException
     */
    public URI run() throws URISyntaxException {
        ProtectionDomain protectionDomain = AuthorizationServlet.class.getProtectionDomain();
        URI location = protectionDomain.getCodeSource().getLocation().toURI();

        return location;
    }
}
