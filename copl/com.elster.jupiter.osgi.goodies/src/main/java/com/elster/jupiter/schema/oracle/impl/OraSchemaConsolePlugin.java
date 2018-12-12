/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.schema.oracle.impl;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

@Component(name = "com.elster.jupiter.schema.oracle.ui", service = Servlet.class, immediate = true,
        property = {"felix.webconsole.label=Ora", "felix.webconsole.title=Oracle schema"})
public class OraSchemaConsolePlugin extends HttpServlet {
    private static final long serialVersionUID = 1L;
    volatile private BundleContext context;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        URL url = context.getBundle().getResource("/js/ora.html");
        PrintWriter writer = response.getWriter();
        InputStream in = url.openConnection().getInputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = in.read(buffer);
        while (bytesRead > 0) {
            writer.write(new String(buffer));
            bytesRead = in.read(buffer);
        }
    }

    @Activate
    void activate(BundleContext context) {
        this.context = context;
    }
}
