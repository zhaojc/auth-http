package helpers.fixture.persistence;

import org.rootservices.authorization.persistence.entity.ResourceOwner;
import org.rootservices.authorization.persistence.repository.ResourceOwnerRepository;
import org.rootservices.authorization.security.HashTextRandomSalt;

import java.util.UUID;

/**
 * Created by tommackenzie on 6/5/15.
 */
public class LoadResourceOwner {

    private HashTextRandomSalt textHasher;
    private ResourceOwnerRepository resourceOwnerRepository;

    public LoadResourceOwner(HashTextRandomSalt textHasher, ResourceOwnerRepository resourceOwnerRepository) {
        this.textHasher = textHasher;
        this.resourceOwnerRepository = resourceOwnerRepository;
    }

    public ResourceOwner run() {
        ResourceOwner ro = new ResourceOwner();
        ro.setUuid(UUID.randomUUID());
        ro.setEmail("test-" + UUID.randomUUID().toString() + "@rootservices.org");

        String hashedPassword = textHasher.run("password");
        ro.setPassword(hashedPassword.getBytes());

        resourceOwnerRepository.insert(ro);
        return ro;
    }
}
