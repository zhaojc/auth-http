package org.rootservices.otter;

import helpers.category.UnitTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.otter.QueryStringToMap;
import org.rootservices.otter.QueryStringToMapImpl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * Created by tommackenzie on 4/22/15.
 */
@Category(UnitTests.class)
public class QueryStringToMapImplTest {

    private QueryStringToMap subject;

    @Before
    public void setUp() {
        subject = new QueryStringToMapImpl();
    }

    @Test
    public void testParamsToMap() throws Exception {
        String decodedQueryString = "param1=value1&param2=value2";
        String encodedQueryString = URLEncoder.encode(decodedQueryString,"UTF-8");
        Optional<String> optionalQueryString = Optional.of(encodedQueryString);

        Map<String, List<String>> params = subject.run(optionalQueryString);
        assertThat(params.get("param1").size()).isEqualTo(1);
        assertThat(params.get("param1").get(0)).isEqualTo("value1");
        assertThat(params.get("param2").size()).isEqualTo(1);
        assertThat(params.get("param2").get(0)).isEqualTo("value2");
    }

    @Test
    public void noQueryParameters() throws UnsupportedEncodingException {
        Optional<String> optionalQueryString = Optional.of("");
        Map<String, List<String>> params = subject.run(optionalQueryString);

        assertThat(params.size()).isEqualTo(0);
    }
}