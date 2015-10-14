package org.rootservices.authorization.http.controller;

import org.rootservices.authorization.grant.code.protocol.authorization.request.ValidateParams;
import org.rootservices.authorization.grant.code.protocol.authorization.exception.AuthCodeInsertException;
import org.rootservices.authorization.grant.code.protocol.authorization.response.AuthCodeInput;
import org.rootservices.authorization.grant.code.protocol.authorization.response.AuthResponse;
import org.rootservices.authorization.grant.code.protocol.authorization.response.RequestAuthCode;
import org.rootservices.authorization.authenticate.exception.UnauthorizedException;
import org.rootservices.authorization.grant.code.exception.InformClientException;
import org.rootservices.authorization.grant.code.exception.InformResourceOwnerException;
import org.rootservices.authorization.grant.openid.protocol.authorization.request.ValidateOpenIdParams;
import org.rootservices.otter.QueryStringToMap;
import org.rootservices.otter.QueryStringToMapImpl;
import org.rootservices.authorization.http.presenter.AuthorizationPresenter;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by tommackenzie on 3/16/15.
 */
@WebServlet(value="/authorization", name="authorizationServlet")
public class AuthorizationServlet extends HttpServlet {

    private static String CLIENT_ID = "client_id";
    private static String RESPONSE_TYPE = "response_type";
    private static String REDIRECT_URI = "redirect_uri";
    private static String SCOPE = "scope";
    private static String STATE = "state";
    private static String EMAIL = "email";
    private static String PASSWORD = "password";
    private static String CSRF_TOKEN = "csrfToken";

    private QueryStringToMap queryStringToMap;
    private ValidateParams validateParams;
    private ValidateOpenIdParams validateOpenIdParams;
    private RequestAuthCode requestAuthCode;

    public AuthorizationServlet() {}

    @Override
    public void init() throws ServletException {
        ApplicationContext context = (ApplicationContext) getServletContext().getAttribute("factory");
        this.queryStringToMap = new QueryStringToMapImpl();
        this.validateParams = context.getBean(ValidateParams.class);
        this.requestAuthCode = context.getBean(RequestAuthCode.class);
        this.validateOpenIdParams = context.getBean(ValidateOpenIdParams.class);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> queryString = Optional.ofNullable(req.getQueryString());
        Map<String, List<String>> parameters = queryStringToMap.run(queryString);

        try {
            if ( isRequestOpenId(parameters.get(SCOPE)) ) {
                validateOpenIdParams.run(
                        parameters.get(CLIENT_ID),
                        parameters.get(RESPONSE_TYPE),
                        parameters.get(REDIRECT_URI),
                        parameters.get(SCOPE),
                        parameters.get(STATE)
                );
            } else {
                validateParams.run(
                        parameters.get(CLIENT_ID),
                        parameters.get(RESPONSE_TYPE),
                        parameters.get(REDIRECT_URI),
                        parameters.get(SCOPE),
                        parameters.get(STATE)
                );
            }
        } catch (InformResourceOwnerException e) {
            req.getRequestDispatcher("notFoundServlet").forward(req, resp);
            return;
        } catch (InformClientException e) {
            resp.setContentType("application/x-www-form-urlencoded");
            String location = e.getRedirectURI() + "?error=" + e.getError();
            resp.sendRedirect(location);
            return;
        }

        AuthorizationPresenter presenter = new AuthorizationPresenter();
        presenter.setEncodedCsrfToken(getEncodedCsrfToken(req));

        req.setAttribute("presenter", presenter);
        req.getRequestDispatcher("/WEB-INF/jsp/authorization.jsp").forward(req, resp);
        return;
    }

    private boolean isRequestOpenId(List<String> scopes) {
        return scopes != null && scopes.contains("openid");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> queryString = Optional.ofNullable(req.getQueryString());
        Map<String, List<String>> parameters = queryStringToMap.run(queryString);

        AuthCodeInput input = new AuthCodeInput();

        // url parameters
        input.setClientIds(parameters.get(CLIENT_ID));
        input.setRedirectUris(parameters.get(REDIRECT_URI));
        input.setResponseTypes(parameters.get(RESPONSE_TYPE));
        input.setScopes(parameters.get(SCOPE));
        input.setStates(parameters.get(STATE));

        // post data
        input.setUserName(req.getParameter(EMAIL));
        input.setPlainTextPassword(req.getParameter(PASSWORD));

        AuthResponse authResponse = null;
        try {
            authResponse = requestAuthCode.run(input);
        } catch (UnauthorizedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            AuthorizationPresenter presenter = new AuthorizationPresenter();
            presenter.setEmail(input.getUserName());
            presenter.setEncodedCsrfToken(getEncodedCsrfToken(req));

            req.setAttribute("presenter", presenter);
            req.getRequestDispatcher("/WEB-INF/jsp/authorization.jsp").forward(req, resp);
            return;
        } catch (InformResourceOwnerException e) {
            req.getRequestDispatcher("notFoundServlet").forward(req, resp);
            return;
        } catch (InformClientException e) {
            resp.setContentType("application/x-www-form-urlencoded");
            String location = e.getRedirectURI() + "?error=" + e.getError();
            resp.sendRedirect(location);
            return;
        } catch (AuthCodeInsertException e) {
            // TODO: pt-99407544 - add logger or notify
        }

        resp.setContentType("application/x-www-form-urlencoded");
        String location = authResponse.getRedirectUri()
                + "?code=" + authResponse.getCode();

        if (authResponse.getState().isPresent()) {
            location += "&state=" + authResponse.getState().get();
        }

        resp.sendRedirect(location);
        return;
    }

    private String getEncodedCsrfToken(HttpServletRequest request) throws UnsupportedEncodingException {
        String csrfToken = (String) request.getSession().getAttribute(CSRF_TOKEN);
        byte[] bytes = csrfToken.getBytes("UTF-8");
        return Base64.getEncoder().encodeToString(bytes);
    }
}
