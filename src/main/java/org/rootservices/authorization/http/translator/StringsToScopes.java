package org.rootservices.authorization.http.translator;

import org.rootservices.authorization.http.translator.exception.ValidationError;
import org.rootservices.authorization.persistence.entity.Scope;

import java.util.List;

/**
 * Created by tommackenzie on 1/17/15.
 */
public interface StringsToScopes {
    public List<Scope> run(List<String> items) throws ValidationError;
}
