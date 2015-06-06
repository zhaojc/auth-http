package org.rootservices.authorization.http;

import helpers.category.UnitTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.authorization.http.controller.AuthorizationServlet;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * Created by tommackenzie on 5/2/15.
 */
@Category(UnitTests.class)
public class GetServletURIImplTest {

    private GetServletURI subject;

    @Before
    public void setUp() {
        subject = new GetServletURIImpl();
    }

    @Test
    public void testRun() throws Exception {
        String actual = subject.run("https://rootservices.org", AuthorizationServlet.class);
        assertThat(actual).isEqualTo("https://rootservices.org/authorization");
    }

    @Test
    public void testRunRemovForwardSlash() throws Exception {
        String actual = subject.run("https://rootservices.org/", AuthorizationServlet.class);
        assertThat(actual).isEqualTo("https://rootservices.org/authorization");
    }
}
