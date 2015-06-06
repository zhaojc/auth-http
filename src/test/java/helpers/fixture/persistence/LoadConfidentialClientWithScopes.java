package helpers.fixture.persistence;

/**
 * Created by tommackenzie on 6/5/15.
 */

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
 * Created by tommackenzie on 5/16/15.
 *
 * sets up database with a client that has scopes.
 *
 * Client        ClientScopes            Scope
 * +uuid   --->  +client_uuid     /--->  +uuid
 *               +scope_uuid  ---/
 */
public class LoadConfidentialClientWithScopes {

    private ClientRepository clientRepository;
    private ScopeRepository scopeRepository;
    private ClientScopesRepository clientScopesRepository;
    private ConfidentialClientRepository confidentialClientRepository;

    public LoadConfidentialClientWithScopes(ClientRepository clientRepository, ScopeRepository scopeRepository, ClientScopesRepository clientScopesRepository, ConfidentialClientRepository confidentialClientRepository) {
        this.clientRepository = clientRepository;
        this.scopeRepository = scopeRepository;
        this.clientScopesRepository = clientScopesRepository;
        this.confidentialClientRepository = confidentialClientRepository;
    }

    public ConfidentialClient run() throws URISyntaxException {
        Client client = EntityFactory.makeClientWithScopes();
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
