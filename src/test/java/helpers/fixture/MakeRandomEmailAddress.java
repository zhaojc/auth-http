package helpers.fixture;

import helpers.suite.IntegrationTestSuite;
import org.rootservices.authorization.grant.code.protocol.token.MakeToken;
import org.rootservices.authorization.security.RandomString;

/**
 * Created by tommackenzie on 6/5/15.
 *
 * Avoids violation of unique key constraint in resource_owner schema.
 * resource_owner.email must be unique.
 */
public class MakeRandomEmailAddress {

    private RandomString randomString;

    public MakeRandomEmailAddress(RandomString randomString) {
        this.randomString = randomString;
    }

    public String run() {
        String user = randomString.run();
        return "auth-http-test-" + user + "@rootservices.org";
    }
}
