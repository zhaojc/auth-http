package org.rootservices.authorization.http.controller;

import org.rootservices.authorization.grant.ValidateParams;
import org.rootservices.authorization.grant.code.exception.InformClientException;
import org.rootservices.authorization.grant.code.exception.InformResourceOwnerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by tommackenzie on 3/16/15.
 */
@WebServlet(value="/authorization", name="authorizationServlet")
public class AuthorizationServlet extends HttpServlet {

    private ValidateParams validateParams;

    public AuthorizationServlet() {}

    @Override
    public void init() throws ServletException {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        this.validateParams = context.getBean(ValidateParams.class);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> queryString = Optional.ofNullable(req.getQueryString());
        Map<String, List<String>> parameters = paramsToMap(queryString);

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
            resp.sendRedirect(e.getRedirectURI().toString());
            return;
        }

        req.getRequestDispatcher("/WEB-INF/jsp/authorization.jsp").forward(req, resp);
        return;
    }

    protected Map<String, List<String>> paramsToMap(Optional<String> queryString) throws UnsupportedEncodingException {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        if ( queryString.isPresent() ) {
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
