package org.rootservices.authorization.http.translator;

import org.rootservices.authorization.http.translator.exception.ValidationError;
import org.rootservices.authorization.http.validator.HasOneItem;
import org.rootservices.authorization.http.validator.IsNotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by tommackenzie on 12/27/14.
 */
@Component
public class StringsToStateImpl implements StringsToState {

    @Autowired
    private HasOneItem hasOneItem;

    @Autowired
    private IsNotNull isNotNull;

    public StringsToStateImpl() {}

    public StringsToStateImpl(IsNotNull isNotNull, HasOneItem hasOneItem) {
        this.isNotNull = isNotNull;
        this.hasOneItem = hasOneItem;
    }

    @Override
    public String run(List<String> items) throws ValidationError {

        if(isNotNull.run(items) == false) {
            throw new ValidationError("parameter is null");
        }

        // optional parameter.
        if( items.size() == 0 ) {
            return null;
        }

        if(hasOneItem.run(items) == false) {
            throw new ValidationError("parameter does not have one item");
        }

        if(items.get(0).isEmpty()) {
            throw new ValidationError("parameter is empty");
        }
        return items.get(0);
    }
}
