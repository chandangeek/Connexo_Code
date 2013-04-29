package com.elster.jupiter.http.whiteboard.impl;

import org.osgi.framework.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.*;
import org.osgi.util.tracker.*;

import com.elster.jupiter.http.whiteboard.HttpResource;

@Component(name = "com.elster.jupiter.http.whiteboard")
public class WhiteBoard {
	private volatile HttpService httpService;
	private volatile ServiceTracker<HttpResource, HttpResource> resourceTracker;
	
	public WhiteBoard() {
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

	@Reference
	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}
	
	@Activate
	public void activate(BundleContext context) {
		this.resourceTracker = new ServiceTracker<>(context, HttpResource.class, new ResourceTrackerCustomizer(context));
		this.resourceTracker.open();
	}

	@Deactivate
	public void deActivate() {
		this.resourceTracker.close();
	}
	
	class ResourceTrackerCustomizer implements ServiceTrackerCustomizer<HttpResource,HttpResource> {
		private final BundleContext bundleContext;
		
		public ResourceTrackerCustomizer(BundleContext bundleContext) {
			this.bundleContext = bundleContext;
		}

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
