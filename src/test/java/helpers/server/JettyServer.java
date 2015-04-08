package helpers.server;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.*;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.eclipse.jetty.util.resource.Resource.newResource;


/**
 * Created by tommackenzie on 3/31/15.
 */
public class JettyServer {
    private Server server;

    public JettyServer() {}

    public JettyServer(Server server) {
        this.server = server;
    }

    public void init(String path, URI webApp, URI classPath) throws MalformedURLException {
        this.server = new Server(0);

        WebAppContext context = new WebAppContext();

        context.setClassLoader(Thread.currentThread().getContextClassLoader());

        Resource resourceBase = newResource(webApp);
        context.setResourceBase(String.valueOf(resourceBase));

        context.setConfigurations(new Configuration[]{
                new WebXmlConfiguration(),
                new AnnotationConfiguration()
        });

        File tempDirectory = new File("/tmp/auth-http");
        context.setTempDirectory(tempDirectory);

        FileResource containerResources = new FileResource(classPath);
        context.getMetaData().addContainerResource(containerResources);

        // web.xml
        String webXmlPath = webApp.getPath() +"/WEB-INF/web.xml";
        context.setDescriptor(webXmlPath);

        context.setContextPath(path);
        context.setParentLoaderPriority(true);

        // Add server context
        server.setHandler(context);
    }

    public void start() throws Exception {
        server.setDumpAfterStart(true);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public URI getURI() {
        return server.getURI();
    }

}

