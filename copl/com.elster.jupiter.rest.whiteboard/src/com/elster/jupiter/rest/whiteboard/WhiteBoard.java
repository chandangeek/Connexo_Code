package com.elster.jupiter.rest.whiteboard;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;

import org.osgi.framework.*;
import org.osgi.service.http.*;
import org.osgi.service.http.HttpContext;
import org.osgi.util.tracker.*;

import com.sun.jersey.api.container.filter.*;
import com.sun.jersey.api.core.*;
import com.sun.jersey.spi.container.*;
import com.sun.jersey.spi.container.servlet.ServletContainer;

class WhiteBoard {
	private final HttpService httpService;
	private final HttpContext httpContext;
	private final BundleContext bundleContext;
	private final ServiceTracker<Application, Application> resourceTracker;
	
	WhiteBoard(BundleContext bundleContext , HttpService httpService) {
		this.httpService = httpService;
		this.bundleContext = bundleContext;
		this.httpContext = new HttpContextImpl();
		this.resourceTracker = new ServiceTracker<>(bundleContext, Application.class, new ApplicationTrackerCustomizer());
	}
	
	void open(final long delay) {
		// some jersey code is dependent on bundle activation, 
		// but does not register any services that can be tracked
		// to avoid complex bundle management, just delay the white board activation
		Runnable runnable = new Runnable() {			
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				open();				
			}
		}; 
		new Thread(runnable).start();
	}
	
	void open() {
		resourceTracker.open();		
	}
	
	void close() {
		resourceTracker.close();
	}
	
	void addResource(String alias, Application application) {
		ResourceConfig secureConfig = new ApplicationAdapter(application);
		List<Class<? extends ContainerRequestFilter>> requestFilters = new ArrayList<>();
		requestFilters.add(RoleFilter.class);
		secureConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,requestFilters);
		List<Class<? extends ResourceFilterFactory>> resourceFilterFactories = new ArrayList<>();
		resourceFilterFactories.add(RolesAllowedResourceFilterFactory.class);
		resourceFilterFactories.add(ResourceDebuggingFilterFactory.class);
		secureConfig.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,resourceFilterFactories);	
		ServletContainer container = new ServletContainer(secureConfig);
		HttpServlet wrapper = new ServletWrapper(container);
		try {
			httpService.registerServlet(alias, wrapper, null , httpContext);
		} catch (ServletException | NamespaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	void removeResource(String alias) {
		httpService.unregister(alias);
	}
	
	class ApplicationTrackerCustomizer implements ServiceTrackerCustomizer<Application, Application> {

		private String getAlias(ServiceReference<Application> reference) {
			return (String) reference.getProperty("alias");
		}
		
		@Override
		public Application addingService(ServiceReference<Application> reference) {
			String alias = getAlias(reference);
			if (alias == null) {
				System.out.println("No alias defined for " + reference);
				return null;
			} else {
				Application application = bundleContext.getService(reference);
				addResource(alias , application);
				return application;
			}
		}

		@Override
		public void modifiedService(ServiceReference<Application> reference, Application service) {			
		}

		@Override
		public void removedService(ServiceReference<Application> reference,Application service) {
			String alias = getAlias(reference);
			if (alias != null) {
				removeResource(alias);
			}
		}

	
	}
}
