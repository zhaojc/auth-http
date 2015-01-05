package org.rootservices.authorization.http.translator;

import java.util.List;

/**
 * Created by tommackenzie on 12/27/14.
 */
public interface StringsToState {
    public String run(List<String> items) throws ValidationError;
}
