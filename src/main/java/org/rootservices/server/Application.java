package org.rootservices.server;
import org.rootservices.authorization.http.controller.Authorization;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;
import org.rootservices.authorization.http.exceptionmapper.NotFoundExceptionMapper;
import org.glassfish.jersey.message.GZipEncoder;

/**
 * Created by tommackenzie on 11/12/14.
 */
public class Application extends ResourceConfig {

    public Application() {
        register(Authorization.class);
        register(NotFoundExceptionMapper.class);

        property(MustacheMvcFeature.TEMPLATE_BASE_PATH, "/templates");
        register(MustacheMvcFeature.class);
        setApplicationName("auth-http");
    }
}