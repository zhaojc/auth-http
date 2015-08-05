package org.rootservices.otter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by tommackenzie on 4/22/15.
 */
public class QueryStringToMapImpl implements QueryStringToMap {

    @Override
    public Map<String, List<String>> run(Optional<String> queryString) throws UnsupportedEncodingException {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        if ( queryString.isPresent() && !queryString.get().isEmpty()) {
            String decoded = URLDecoder.decode(queryString.get(), "UTF-8");
            String[] pares = decoded.split("&");

            for (String pare : pares) {
                String[] nameAndValue = pare.split("=");
                List<String> items;
                if (parameters.containsKey(nameAndValue[0])) {
                    items = parameters.get(nameAndValue[0]);
                } else {
                    items = new ArrayList<>();

                }
                items.add(nameAndValue[1]);
                parameters.put(nameAndValue[0], items);
            }
        }
        return parameters;
    }
}
