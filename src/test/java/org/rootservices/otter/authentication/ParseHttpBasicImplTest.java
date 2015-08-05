package org.rootservices.otter.authentication;

import helpers.category.UnitTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.otter.authentication.exception.HttpBasicException;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * Created by tommackenzie on 6/4/15.
 *
 */
@Category(UnitTests.class)
public class ParseHttpBasicImplTest {

    private ParseHttpBasic subject;

    @Before
    public void setUp() {
        subject = new ParseHttpBasicImpl();
    }
    @Test
    public void testRun() throws HttpBasicException, UnsupportedEncodingException {
        String credentials = "user:password";
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpBasicEntity actual = subject.run("Basic " + encodedCredentials);
        assertThat(actual.getUser()).isEqualTo("user");
        assertThat(actual.getPassword()).isEqualTo("password");
    }

    @Test(expected=HttpBasicException.class)
    public void testHeaderIsEmpty() throws HttpBasicException {
        subject.run("");
    }

    @Test(expected=HttpBasicException.class)
    public void testHeaderIsNull() throws HttpBasicException {
        subject.run(null);
    }

    @Test(expected=HttpBasicException.class)
    public void testHeaderIsNotBasic() throws HttpBasicException {
        subject.run("foo");
    }

    @Test(expected=HttpBasicException.class)
    public void testHeaderMissingCredentials() throws HttpBasicException {
        subject.run("Basic ");
    }

    @Test(expected=HttpBasicException.class)
    public void testHeaderHasNoColon() throws HttpBasicException {
        String garbage = Base64.getEncoder().encodeToString("gabage".getBytes());
        subject.run("Basic " + garbage);
    }

    @Test(expected=HttpBasicException.class)
    public void testHeaderMissingPassword() throws HttpBasicException {
        String missingPassword = Base64.getEncoder().encodeToString("user:".getBytes());
        subject.run("Basic " + missingPassword);
    }

}