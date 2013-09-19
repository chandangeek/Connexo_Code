package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.rest.whiteboard.RestCallExecutedEvent;
import com.elster.jupiter.util.time.StopWatch;
import java.util.logging.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

public class EventServletWrapper extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final HttpServlet servlet;

    public EventServletWrapper(HttpServlet servlet) {
        this.servlet = servlet;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        servlet.init(getServletConfig());
    }

    public void destroy() {
        servlet.destroy();
        super.destroy();
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        StopWatch stopWatch = new StopWatch(true);
        servlet.service(request,response);
        stopWatch.stop();
        URL url = new URL(request.getRequestURL().toString());
        RestCallExecutedEvent event = new RestCallExecutedEvent(url, stopWatch);
        Bus.fire(event);
        Logger.getLogger("com.elster.jupiter.rest.whiteboard").info("" + event);
    }

}
