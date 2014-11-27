package org.rootservices.authorization.http.builder;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.rootservices.authorization.http.response.OkResponse;

import static org.fest.assertions.api.Assertions.assertThat;

public class OkResponseBuilderImplTest {

    private OkResponseBuilder subject;

    @Before
    public void setUp() {
        subject = new OkResponseBuilderImpl();
    }

    @Test
    public void buildOkResponse() {
        OkResponse OR = (OkResponse) subject.buildOkResponse();
        assertThat(OR instanceof OkResponse).isEqualTo(true);
    }

}