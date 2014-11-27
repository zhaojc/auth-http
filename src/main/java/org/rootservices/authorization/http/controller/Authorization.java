package org.rootservices.authorization.http.controller;

import org.rootservices.authorization.http.exception.NotFoundException;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Path;

/**
 * Created by tommackenzie on 11/27/14.
 */
@Controller
@Path("authorization")
public class Authorization extends AbstractAuthorization {

    @Override
    protected String getTemplateName() {
        return "/authorization.mustache";
    }
}
