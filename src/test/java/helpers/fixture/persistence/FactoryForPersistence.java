package helpers.fixture.persistence;

import helpers.suite.IntegrationTestSuite;
import org.rootservices.authorization.persistence.repository.*;
import org.rootservices.authorization.security.HashTextRandomSalt;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by tommackenzie on 6/5/15.
 */
public class FactoryForPersistence {
    private ClassPathXmlApplicationContext context;

    public FactoryForPersistence(ClassPathXmlApplicationContext context) {
        this.context = context;
    }

    public LoadConfidentialClientWithScopes makeLoadConfidentialClientWithScopes() {
        ClientRepository clientRepository = IntegrationTestSuite.getContext().getBean(ClientRepository.class);
        ConfidentialClientRepository confidentialClientRepository = confidentialClientRepository = IntegrationTestSuite.getContext().getBean(ConfidentialClientRepository.class);
        ScopeRepository scopeRepository = scopeRepository = IntegrationTestSuite.getContext().getBean(ScopeRepository.class);
        ClientScopesRepository clientScopesRepository = clientScopesRepository = IntegrationTestSuite.getContext().getBean(ClientScopesRepository.class);

        LoadConfidentialClientWithScopes loadConfidentialClientWithScopes = new LoadConfidentialClientWithScopes(
                clientRepository, scopeRepository, clientScopesRepository, confidentialClientRepository
        );

        return loadConfidentialClientWithScopes;
    }

    public LoadResourceOwner makeLoadResourceOwner() {

        // resource owner persistence.
        HashTextRandomSalt textHasher = IntegrationTestSuite.getContext().getBean(HashTextRandomSalt.class);
        ResourceOwnerRepository resourceOwnerRepository = IntegrationTestSuite.getContext().getBean(ResourceOwnerRepository.class);
        LoadResourceOwner loadResourceOwner = new LoadResourceOwner(textHasher, resourceOwnerRepository);

        return loadResourceOwner;
    }
}
