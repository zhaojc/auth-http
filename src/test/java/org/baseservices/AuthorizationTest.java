package org.baseservices;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AuthorizationTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Authorization.class);
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void authorize() {
        String response = target().path("authorization").request().get(String.class);
        assertEquals("hello world!", response);
    }
}
