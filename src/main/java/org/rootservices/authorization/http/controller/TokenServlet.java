package org.rootservices.authorization.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rootservices.authorization.authenticate.exception.UnauthorizedException;
import org.rootservices.authorization.grant.code.protocol.token.RequestToken;
import org.rootservices.authorization.grant.code.protocol.token.TokenInput;
import org.rootservices.authorization.grant.code.protocol.token.TokenResponse;
import org.rootservices.authorization.grant.code.protocol.token.exception.AuthorizationCodeNotFound;
import org.rootservices.authorization.grant.code.protocol.token.exception.BadRequestException;
import org.rootservices.authorization.grant.code.protocol.token.exception.CompromisedCodeException;
import org.rootservices.otter.authentication.HttpBasicEntity;
import org.rootservices.otter.authentication.ParseHttpBasic;
import org.rootservices.otter.authentication.ParseHttpBasicImpl;
import org.rootservices.otter.authentication.exception.HttpBasicException;
import org.rootservices.authorization.http.response.Error;
import org.springframework.context.ApplicationContext;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Created by tommackenzie on 6/3/15.
 */
@WebServlet(value="/token", name="tokenServlet")
public class TokenServlet extends HttpServlet {
    private RequestToken requestToken;
    private ParseHttpBasic parseHttpBasic;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        ApplicationContext context = (ApplicationContext) getServletContext().getAttribute("factory");
        requestToken = context.getBean(RequestToken.class);
        parseHttpBasic = new ParseHttpBasicImpl();
        objectMapper = context.getBean(ObjectMapper.class);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String authenticationHeader = req.getHeader("Authorization");
        HttpBasicEntity httpBasicEntity = null;
        try {
            httpBasicEntity = parseHttpBasic.run(authenticationHeader);
        } catch (HttpBasicException e) {
            Error error = new Error("invalid_client", null);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setHeader("WWW-Authenticate", "Basic");
            setResponseHeaders(resp);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        TokenInput tokenInput = new TokenInput();
        tokenInput.setClientUUID(httpBasicEntity.getUser());
        tokenInput.setClientPassword(httpBasicEntity.getPassword());
        tokenInput.setPayload(req.getReader());

        TokenResponse tokenResponse = null;
        try {
            tokenResponse = requestToken.run(tokenInput);
        } catch(UnauthorizedException e) {
            Error error = new Error("invalid_client", null);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setHeader("WWW-Authenticate", "Basic");
            setResponseHeaders(resp);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        } catch (AuthorizationCodeNotFound e) {
            Error error = new Error(e.getError(), null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            setResponseHeaders(resp);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        } catch (BadRequestException e) {
            Error error = new Error(e.getError(), e.getDescription());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            setResponseHeaders(resp);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        } catch (CompromisedCodeException e) {
            Error error = new Error(e.getError(), null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            setResponseHeaders(resp);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        }

        setResponseHeaders(resp);
        resp.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
        return;
    }

    private void setResponseHeaders(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Pragma", "no-cache");
    }
}


