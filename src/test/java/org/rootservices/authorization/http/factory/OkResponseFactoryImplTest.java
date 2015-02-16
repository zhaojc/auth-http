package org.rootservices.authorization.http.factory;

import org.junit.Before;
import org.junit.Test;
import org.rootservices.authorization.http.factory.OkResponseFactory;
import org.rootservices.authorization.http.factory.OkResponseFactoryImpl;
import org.rootservices.authorization.http.response.OkResponse;

import static org.fest.assertions.api.Assertions.assertThat;

public class OkResponseFactoryImplTest {

    private OkResponseFactory subject;

    @Before
    public void setUp() {
        subject = new OkResponseFactoryImpl();
    }

    @Test
    public void buildOkResponse() {
        OkResponse OR = (OkResponse) subject.buildOkResponse();
        assertThat(OR instanceof OkResponse).isEqualTo(true);
    }

}