package org.rootservices.authorization.http.translator;

import java.util.List;
import java.util.UUID;

/**
 * Created by tommackenzie on 12/13/14.
 */
public interface StringsToUUID {
    public UUID run(List<String> items) throws ValidationError;
}
