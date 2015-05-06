package org.rootservices.authorization.http.controller;

import org.rootservices.authorization.grant.ValidateParams;
import org.rootservices.authorization.grant.code.authenticate.AuthCodeInput;
import org.rootservices.authorization.grant.code.authenticate.AuthResponse;
import org.rootservices.authorization.grant.code.authenticate.RequestAuthCode;
import org.rootservices.authorization.grant.code.authenticate.exception.UnauthorizedException;
import org.rootservices.authorization.grant.code.exception.InformClientException;
import org.rootservices.authorization.grant.code.exception.InformResourceOwnerException;
import org.rootservices.authorization.grant.code.request.AuthRequest;
import org.rootservices.authorization.http.QueryStringToMap;
import org.rootservices.authorization.http.QueryStringToMapImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by tommackenzie on 3/16/15.
 */
@WebServlet(value="/authorization", name="authorizationServlet")
public class AuthorizationServlet extends HttpServlet {

    private QueryStringToMap queryStringToMap;
    private ValidateParams validateParams;
    private RequestAuthCode requestAuthCode;

    public AuthorizationServlet() {}

    @Override
    public void init() throws ServletException {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        this.queryStringToMap = new QueryStringToMapImpl();
        this.validateParams = context.getBean(ValidateParams.class);
        this.requestAuthCode = context.getBean(RequestAuthCode.class);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> queryString = Optional.ofNullable(req.getQueryString());
        Map<String, List<String>> parameters = queryStringToMap.run(queryString);

        try {
            validateParams.run(
                    parameters.get("client_id"),
                    parameters.get("response_type"),
                    parameters.get("redirect_uri"),
                    parameters.get("scope"),
                    parameters.get("state")
            );
        } catch (InformResourceOwnerException e) {
            req.getRequestDispatcher("notFoundServlet").forward(req, resp);
            return;
        } catch (InformClientException e) {
            resp.setContentType("application/x-www-form-urlencoded");
            resp.sendRedirect(e.getRedirectURI().toString());
            return;
        }

        req.getRequestDispatcher("/WEB-INF/jsp/authorization.jsp").forward(req, resp);
        return;
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> queryString = Optional.ofNullable(req.getQueryString());
        Map<String, List<String>> parameters = queryStringToMap.run(queryString);

        AuthCodeInput input = new AuthCodeInput();

        // url parameters
        input.setClientIds(parameters.get("client_id"));
        input.setRedirectUris(parameters.get("redirect_uri"));
        input.setResponseTypes(parameters.get("response_type"));
        input.setScopes(parameters.get("scope"));
        input.setStates(parameters.get("state"));

        // post data
        input.setUserName(req.getParameter("email"));
        input.setPlainTextPassword(req.getParameter("password"));

        AuthResponse authResponse = null;
        try {
            authResponse = requestAuthCode.run(input);
        } catch (UnauthorizedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (InformResourceOwnerException e) {
            req.getRequestDispatcher("notFoundServlet").forward(req, resp);
            return;
        } catch (InformClientException e) {
            resp.setContentType("application/x-www-form-urlencoded");
            String location = e.getRedirectURI() + "?error=" + e.getError();
            resp.sendRedirect(location);
            return;
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
}
