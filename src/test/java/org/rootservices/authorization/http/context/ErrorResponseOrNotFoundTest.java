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
public class ErrorResponseOrNotFoundTest {

    private static int FOUND = Status.FOUND.getStatusCode();

    @Mock
    private GetClientRedirectURI mockGetClientRedirectURI;

    private ErrorResponseOrNotFound subject;

    @Before
    public void setUp() {
        subject = new ErrorResponseOrNotFound();
        ReflectionTestUtils.setField(subject, "getClientRedirectURI", mockGetClientRedirectURI);
    }

    @Test
    public void run() throws URISyntaxException, RecordNotFoundException, NotFoundException {
        UUID clientId = UUID.randomUUID();
        URI redirectURI = new URI("https://rootservices.org");
        when(mockGetClientRedirectURI.run(clientId)).thenReturn(redirectURI);

        Response actual = subject.run(clientId);
        assertThat(actual.getStatus()).isEqualTo(FOUND);
    }
}