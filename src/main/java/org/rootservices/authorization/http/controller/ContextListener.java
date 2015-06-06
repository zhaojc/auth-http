package org.rootservices.authorization.http.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by tommackenzie on 6/3/15.
 */
@WebListener
public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext c = servletContextEvent.getServletContext();
        if (c != null) {
            if (c.getInitParameter("factory") == null) {
                ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
                c.setAttribute("factory", context);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletContext c = servletContextEvent.getServletContext();
        if (c.getAttribute("factory") != null) {
            c.removeAttribute("factory");
        }
    }
}
