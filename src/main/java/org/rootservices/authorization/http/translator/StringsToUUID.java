package org.rootservices.authorization.http.translator;

import org.rootservices.authorization.http.translator.exception.ValidationError;

import java.util.List;
import java.util.UUID;

/**
 * Created by tommackenzie on 12/13/14.
 */
public interface StringsToUUID {
    public UUID run(List<String> items) throws ValidationError;
}
