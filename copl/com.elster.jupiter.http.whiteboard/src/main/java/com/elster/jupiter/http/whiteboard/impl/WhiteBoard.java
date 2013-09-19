package com.elster.jupiter.http.whiteboard.impl;

import java.util.HashMap;

import org.osgi.service.component.annotations.*;
import org.osgi.service.http.*;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.elster.jupiter.http.whiteboard.HttpResource;

@Component(name = "com.elster.jupiter.http.whiteboard")
public class WhiteBoard {
	private volatile HttpService httpService;
	
	public WhiteBoard() {
	}
	
	@Reference
	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}
	
	@Reference(name = "ZResource" , cardinality = ReferenceCardinality.MULTIPLE , policy = ReferencePolicy.DYNAMIC)
	public void addResource(HttpResource resource) {
		HttpContext httpContext = new HttpContextImpl(resource.getResolver());
		try {
			httpService.registerResources(getAlias(resource.getAlias()),resource.getLocalName(), httpContext);
		} catch (NamespaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void removeResource(HttpResource resource) {		
		httpService.unregister(getAlias(resource.getAlias()));
	}
	
	private String getAlias(String name) {
		return "/js" + name;
	}

}
