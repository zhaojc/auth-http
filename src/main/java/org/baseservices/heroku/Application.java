package org.baseservices.heroku;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;

/**
 * Created by tommackenzie on 11/12/14.
 */
public class Application extends ResourceConfig {

    public Application() {
        packages("org.baseservices");
        property(MustacheMvcFeature.TEMPLATE_BASE_PATH, "/templates");
        register(MustacheMvcFeature.class);
        setApplicationName("auth-http");
    }
}