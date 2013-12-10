package com.elster.jupiter.http.whiteboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.*;
import org.osgi.service.http.*;

import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.StartPage;
import com.elster.jupiter.rest.util.BinderProvider;
import com.google.common.collect.ImmutableSet;

@Component(name = "com.elster.jupiter.http.whiteboard", service=Application.class, property = {"alias=/apps"})
public class WhiteBoard extends Application implements BinderProvider {
	private volatile HttpService httpService;
	private List<HttpResource> resources = Collections.synchronizedList(new ArrayList<HttpResource>());
	
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
			resources.add(resource);
		} catch (NamespaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void removeResource(HttpResource resource) {		
		httpService.unregister(getAlias(resource.getAlias()));
		resources.remove(resource);
	}
	
	String getAlias(String name) {
		return "/js" + name;
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return ImmutableSet.<Class<?>>of(PageResource.class);
	}

	List<HttpResource> getResources() {
		synchronized (resources) {
			return new ArrayList<>(resources);
		}
	}

	@Override
	public Binder getBinder() {
		return new AbstractBinder() {	
			@Override
			protected void configure() {
				this.bind(WhiteBoard.this).to(WhiteBoard.class);
			}
		};
	}
}

	
