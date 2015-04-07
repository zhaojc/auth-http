package helpers.suite;

import com.ning.http.client.AsyncHttpClient;
import helpers.path.GetCompiledClassesPath;
import helpers.path.GetWebAppPathFromClassesURI;
import helpers.category.ServletContainer;
import helpers.server.JettyServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.rootservices.authorization.http.controller.AuthorizationServletTest;

import java.net.URI;

/**
 * Created by tommackenzie on 4/1/15.
 */
@RunWith(Categories.class)
@Categories.IncludeCategory(ServletContainer.class)
@Suite.SuiteClasses({AuthorizationServletTest.class})
public class IntegrationTestSuite {

    private static JettyServer server;
    private static AsyncHttpClient httpClient;
    private static GetCompiledClassesPath getCompiledClassesPath;
    private static GetWebAppPathFromClassesURI getWebAppPathFromClassesURI;

    /**
     * Starts a servlet container.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() throws Exception {

        // dependencies to aid configuration of servlet container
        getCompiledClassesPath = new GetCompiledClassesPath();
        getWebAppPathFromClassesURI = new GetWebAppPathFromClassesURI();

        // auth-http absolute paths used in configuring servlet container
        URI classesPath = getCompiledClassesPath.run();
        URI webAppPath = getWebAppPathFromClassesURI.run(classesPath);

        // start the servlet container.
        server = new JettyServer();
        server.init("/", webAppPath, classesPath);
        server.start();

        httpClient = new AsyncHttpClient();
    }

    /**
     * Stops a servlet container
     *
     * @throws Exception
     */
    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }

    public static JettyServer getServer() {
        return server;
    }

    public static void setServer(JettyServer server) {
        IntegrationTestSuite.server = server;
    }

    public static AsyncHttpClient getHttpClient() {
        return httpClient;
    }

    public static void setHttpClient(AsyncHttpClient httpClient) {
        IntegrationTestSuite.httpClient = httpClient;
    }
}
