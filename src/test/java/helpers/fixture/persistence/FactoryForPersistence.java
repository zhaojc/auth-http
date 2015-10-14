package helpers.fixture.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.suite.IntegrationTestSuite;
import org.rootservices.authorization.persistence.repository.*;
import org.rootservices.authorization.security.HashTextRandomSalt;
import org.rootservices.config.AppConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by tommackenzie on 6/5/15.
 */
public class FactoryForPersistence {
    private ClassPathXmlApplicationContext context;

    public FactoryForPersistence(ClassPathXmlApplicationContext context) {
        this.context = context;
    }

    public ClientRepository makeClientRepository() {
        return IntegrationTestSuite.getContext().getBean(ClientRepository.class);
    }

    public ConfidentialClientRepository makeConfidentialClientRepository() {
        return IntegrationTestSuite.getContext().getBean(ConfidentialClientRepository.class);
    }

    public ScopeRepository makeScopeRepository() {
        return IntegrationTestSuite.getContext().getBean(ScopeRepository.class);
    }

    public ClientScopesRepository makeClientScopesRepository() {
        return IntegrationTestSuite.getContext().getBean(ClientScopesRepository.class);
    }

    public LoadConfidentialClientWithScopes makeLoadConfidentialClientWithScopes() {
        return new LoadConfidentialClientWithScopes(
                makeClientRepository(),
                makeScopeRepository(),
                makeClientScopesRepository(),
                makeConfidentialClientRepository()
        );
    }

    public LoadOpenIdConfidentialClientWithScopes makeLoadOpenIdConfidentialClientWithScopes() {
        return new LoadOpenIdConfidentialClientWithScopes(
                makeClientRepository(),
                makeScopeRepository(),
                makeClientScopesRepository(),
                makeConfidentialClientRepository()
        );
    }

    public LoadResourceOwner makeLoadResourceOwner() {

        // resource owner persistence.
        HashTextRandomSalt textHasher = IntegrationTestSuite.getContext().getBean(HashTextRandomSalt.class);
        ResourceOwnerRepository resourceOwnerRepository = IntegrationTestSuite.getContext().getBean(ResourceOwnerRepository.class);
        LoadResourceOwner loadResourceOwner = new LoadResourceOwner(textHasher, resourceOwnerRepository);

        return loadResourceOwner;
    }

    public GetSessionAndCsrfToken makeGetSessionAndCsrfToken() {
        return new GetSessionAndCsrfToken(IntegrationTestSuite.getHttpClient());
    }

    public PostAuthorizationForm makePostAuthorizationForm() {
        return new PostAuthorizationForm(
                IntegrationTestSuite.getHttpClient(),
                makeLoadResourceOwner(),
                makeGetSessionAndCsrfToken()
        );
    }

    public GetToken makeGetToken() {
        AppConfig config = new AppConfig();
        return new GetToken(
                IntegrationTestSuite.getHttpClient(),
                config.objectMapper()
        );
    }
}
