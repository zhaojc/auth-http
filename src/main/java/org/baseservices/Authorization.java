package org.baseservices;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.glassfish.jersey.server.mvc.Template;

@Path("authorization")
public class Authorization {

    @GET
    @Produces("text/html")
    @Template(name="/authorization.mustache")
    public String authorize() {
        return "hello world!";
    }
}
