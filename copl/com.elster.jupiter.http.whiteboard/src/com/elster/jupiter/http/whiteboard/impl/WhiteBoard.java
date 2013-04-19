package com.elster.jupiter.http.whiteboard.impl;

import org.osgi.framework.*;
import org.osgi.service.http.*;
import org.osgi.util.tracker.*;

import com.elster.jupiter.http.whiteboard.HttpResource;

class WhiteBoard {
	private final HttpService httpService;
	private final BundleContext bundleContext;
	private final ServiceTracker<HttpResource, HttpResource> resourceTracker;
	
	WhiteBoard(BundleContext bundleContext , HttpService httpService) {
		this.httpService = httpService;
		this.bundleContext = bundleContext;
		this.resourceTracker = new ServiceTracker<>(bundleContext, HttpResource.class, new ResourceTrackerCustomizer());
	}
	
	void open() {
		resourceTracker.open();		
	}
	
	void close() {
		resourceTracker.close();
	}
	
	void addResource(HttpResource resource) {
		HttpContext httpContext = new HttpContextImpl(resource.getResolver());
		try {
			httpService.registerResources(resource.getAlias(),resource.getLocalName(), httpContext);
		} catch (NamespaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	void removeResource(HttpResource resource) {		
		httpService.unregister(resource.getAlias());
	}
	
	class ResourceTrackerCustomizer implements ServiceTrackerCustomizer<HttpResource,HttpResource> {

		@Override
		public HttpResource addingService(ServiceReference<HttpResource> reference) {
			HttpResource resource = bundleContext.getService(reference);
			addResource(resource);
			return resource;
		}

		@Override
		public void modifiedService(ServiceReference<HttpResource> reference, HttpResource service) {						
		}

		@Override
		public void removedService(ServiceReference<HttpResource> reference,HttpResource service) {
			removeResource(service);
		}
	}
}
