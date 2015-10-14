package helpers.fixture.persistence;

import helpers.fixture.EntityFactory;
import org.rootservices.authorization.persistence.entity.Client;
import org.rootservices.authorization.persistence.entity.ClientScope;
import org.rootservices.authorization.persistence.entity.ConfidentialClient;
import org.rootservices.authorization.persistence.entity.Scope;
import org.rootservices.authorization.persistence.repository.ClientRepository;
import org.rootservices.authorization.persistence.repository.ClientScopesRepository;
import org.rootservices.authorization.persistence.repository.ConfidentialClientRepository;
import org.rootservices.authorization.persistence.repository.ScopeRepository;

import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Created by tommackenzie on 10/13/15.
 */
public class LoadOpenIdConfidentialClientWithScopes {
    private ClientRepository clientRepository;
    private ScopeRepository scopeRepository;
    private ClientScopesRepository clientScopesRepository;
    private ConfidentialClientRepository confidentialClientRepository;

    public LoadOpenIdConfidentialClientWithScopes(ClientRepository clientRepository, ScopeRepository scopeRepository, ClientScopesRepository clientScopesRepository, ConfidentialClientRepository confidentialClientRepository) {
        this.clientRepository = clientRepository;
        this.scopeRepository = scopeRepository;
        this.clientScopesRepository = clientScopesRepository;
        this.confidentialClientRepository = confidentialClientRepository;
    }

    public ConfidentialClient run() throws URISyntaxException {
        Client client = EntityFactory.makeOpenIdClientWithScopes();
        clientRepository.insert(client);

        ConfidentialClient confidentialClient = EntityFactory.makeConfidentialClient(client);
        confidentialClientRepository.insert(confidentialClient);

        for (Scope scope: client.getScopes()) {
            scopeRepository.insert(scope);
            ClientScope clientScope = new ClientScope(
                    UUID.randomUUID(), client.getUuid(), scope.getUuid()
            );
            clientScopesRepository.insert(clientScope);
        }
        return confidentialClient;
    }
}
