package org.rootservices.authorization.http.validator;

import java.util.List;

/**
 * Created by tommackenzie on 12/13/14.
 */
public interface HasOneItem {
    public boolean run(List items);
}
