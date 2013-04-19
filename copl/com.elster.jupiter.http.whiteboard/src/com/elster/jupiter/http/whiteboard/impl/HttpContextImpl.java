package com.elster.jupiter.http.whiteboard.impl;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;

import com.elster.jupiter.http.whiteboard.Resolver;

public class HttpContextImpl implements HttpContext {
	
	private final Resolver resolver;
	
	HttpContextImpl(Resolver resolver) {
		this.resolver = resolver;
	}
	
	@Override
	public String getMimeType(String arg0) {
		return null;
	}

	@Override
	public URL getResource(String name) {
		return resolver.getResource(name);
	}

	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return true;
	}

}
