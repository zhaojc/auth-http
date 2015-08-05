package org.rootservices.otter.security.csrf.synchronizerToken.filter;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by tommackenzie on 7/30/15.
 */
public class CsrfPreventionFilter implements Filter {

    private final String POST = "POST";
    private final String PUT = "PUT";
    private final String DELETE = "DELETE";
    private final String CHALLENGE_TOKEN_SESSION_NAME = "csrfToken";
    private final String CHALLENGE_TOKEN_FORM_NAME = "csrfToken";

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Optional<String> sessionChallengeToken = getSessionChallengeToken(httpRequest);

        if (requiresChallengeToken(httpRequest.getMethod())) {
            Optional<String> formChallengeToken = getFormChallengeToken(httpRequest);

            if ( (!sessionChallengeToken.isPresent() || !formChallengeToken.isPresent() ) ||
                    (sessionChallengeToken.isPresent() && formChallengeToken.isPresent() &&
                     !sessionChallengeToken.get().equals(formChallengeToken.get()))) {

                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

        } else if (!sessionChallengeToken.isPresent()) {
            insertSessionChallengeToken(httpRequest);
        }

        chain.doFilter(httpRequest, response);
    }

    private boolean requiresChallengeToken(String method) {
        return (method == POST || method == PUT || method == DELETE);
    }

    private Optional<String> getSessionChallengeToken(HttpServletRequest request) {
        return Optional.ofNullable((String) request.getSession().getAttribute(CHALLENGE_TOKEN_SESSION_NAME));
    }

    private void insertSessionChallengeToken(HttpServletRequest request) {
        request.getSession().setAttribute(CHALLENGE_TOKEN_SESSION_NAME, "challenge-token");
    }

    private Optional<String> getFormChallengeToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(CHALLENGE_TOKEN_FORM_NAME));
    }

    @Override
    public void destroy() {
        filterConfig = null;
    }
}
