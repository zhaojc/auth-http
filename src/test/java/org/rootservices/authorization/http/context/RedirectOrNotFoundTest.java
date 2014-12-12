package org.rootservices.authorization.http.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.rootservices.authorization.context.GetClientRedirectURI;
import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.persistence.exceptions.RecordNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedirectOrNotFoundTest {

    private static int MOVED_PERMANENTLY = Status.MOVED_PERMANENTLY.getStatusCode();

    @Mock
    private GetClientRedirectURI mockGetClientRedirectURI;

    private RedirectOrNotFound subject;

    @Before
    public void setUp() {
        subject = new RedirectOrNotFound();
        ReflectionTestUtils.setField(subject, "getClientRedirectURI", mockGetClientRedirectURI);
    }

    @Test
    public void run() throws URISyntaxException, RecordNotFoundException, NotFoundException {
        UUID uuid = UUID.randomUUID();
        URI redirectURI = new URI("https://rootservices.org");
        when(mockGetClientRedirectURI.run(uuid)).thenReturn(redirectURI);

        Response actual = subject.run(uuid.toString());
        assertThat(actual.getStatus()).isEqualTo(MOVED_PERMANENTLY);
    }
}