package org.rootservices.authorization.http.filter;

import javax.servlet.annotation.WebFilter;
import org.rootservices.otter.security.csrf.synchronizerToken.filter.CsrfPreventionFilter;

/**
 * Created by tommackenzie on 8/5/15.
 */
@WebFilter(
        filterName = "csrfProhibitFilter",
        servletNames="authorizationServlet"
)
public class CsrfProhibitFilter extends CsrfPreventionFilter {
}
