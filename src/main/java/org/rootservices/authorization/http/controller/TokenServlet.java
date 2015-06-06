package org.rootservices.authorization.http.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.rootservices.authorization.authenticate.exception.UnauthorizedException;
import org.rootservices.authorization.exception.BaseInformException;
import org.rootservices.authorization.grant.code.protocol.token.RequestToken;
import org.rootservices.authorization.grant.code.protocol.token.TokenRequest;
import org.rootservices.authorization.grant.code.protocol.token.TokenResponse;
import org.rootservices.authorization.http.authentication.HttpBasicEntity;
import org.rootservices.authorization.http.authentication.ParseHttpBasic;
import org.rootservices.authorization.http.authentication.ParseHttpBasicImpl;
import org.rootservices.authorization.http.authentication.exception.HttpBasicException;
import org.springframework.context.ApplicationContext;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;


/**
 * Created by tommackenzie on 6/3/15.
 */
@WebServlet(value="/token", name="tokenServlet")
public class TokenServlet extends HttpServlet {
    private RequestToken requestToken;
    private ParseHttpBasic parseHttpBasic;
    private Gson jsonMarsal;


    @Override
    public void init() throws ServletException {
        ApplicationContext context = (ApplicationContext) getServletContext().getAttribute("factory");
        requestToken = context.getBean(RequestToken.class);
        parseHttpBasic = new ParseHttpBasicImpl();
        jsonMarsal = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String authenticationHeader = req.getHeader("Authorization");
        HttpBasicEntity httpBasicEntity = null;
        try {
            httpBasicEntity = parseHttpBasic.run(authenticationHeader);
        } catch (HttpBasicException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setHeader("WWW-Authenticate", "Basic");
            return;
        }

        BufferedReader reader = req.getReader();
        TokenRequest tokenRequest = jsonMarsal.fromJson(reader, TokenRequest.class);

        tokenRequest.setClientUUID(httpBasicEntity.getUser());
        tokenRequest.setClientPassword(httpBasicEntity.getPassword());

        TokenResponse tokenResponse = null;
        try {
            tokenResponse = requestToken.run(tokenRequest);
        } catch(UnauthorizedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (BaseInformException e) {
            e.printStackTrace();
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Pragma", "no-cache");
        resp.getWriter().write(jsonMarsal.toJson(tokenResponse));
        return;
    }
}


