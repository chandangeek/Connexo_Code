/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import java.io.*;
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.http.*;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

@Component(name="com.elster.jupiter.osgi.goodies.datamodel", service=Servlet.class, immediate=true, property={ "felix.webconsole.label=DataModel" ,  "felix.webconsole.title=DataModel Browser" })
public class DataModelConsolePlugin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	volatile private BundleContext context;
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URL url = context.getBundle().getResource("/js/datamodel.html");
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
