package org.rootservices.authorization.http.factory;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.rootservices.authorization.http.exception.NotFoundException;
import org.rootservices.authorization.http.response.NotFoundResponse;

import static org.fest.assertions.api.Assertions.assertThat;

public class NotFoundResponseFactoryImplTest {

    private NotFoundResponseFactory subject;

    @Before
    public void setUp() {
        subject = new NotFoundResponseFactoryImpl();
    }

    @Test
    public void createNFR() {
        Throwable t = new Throwable();
        NotFoundException NFE = new NotFoundException("Entity Not found", t);

        NotFoundResponse response = (NotFoundResponse) subject.createNFR(NFE);
        assertThat("Not Found").isEqualTo(response.message);
    }


}