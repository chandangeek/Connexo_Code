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
	private volatile EventAdmin eventAdmin;
	
	public WhiteBoard() {
	}
	
	@Reference
	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}
	
	@Reference
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE , policy = ReferencePolicy.DYNAMIC)
	public void addResource(HttpResource resource) {
		HttpContext httpContext = new HttpContextImpl(resource.getResolver());
		try {
			httpService.registerResources(resource.getAlias(),resource.getLocalName(), httpContext);
			Event event = new Event("com/elster/jupiter/http/whiteboard", new HashMap<String,Object>());
			eventAdmin.postEvent(event);
		} catch (NamespaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void removeResource(HttpResource resource) {		
		httpService.unregister(resource.getAlias());
	}

}
