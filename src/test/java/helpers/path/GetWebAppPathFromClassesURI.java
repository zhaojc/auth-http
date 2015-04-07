package helpers.path;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by tommackenzie on 4/1/15.
 */
public class GetWebAppPathFromClassesURI {

    /**
     * When a URI to a project's, target/classes location is provided
     * Then return a URI to the project's webapp location.
     *
     * @param compiledClassesPath
     * @return
     * @throws URISyntaxException
     */
    public URI run(URI compiledClassesPath) throws URISyntaxException {
        String projectPath = compiledClassesPath.getPath().split("/target")[0];
        String webAppPath = "file:" + projectPath + "/src/main/webapp";
        URI webAppURI = new URI(webAppPath);

        return webAppURI;
    }
}
