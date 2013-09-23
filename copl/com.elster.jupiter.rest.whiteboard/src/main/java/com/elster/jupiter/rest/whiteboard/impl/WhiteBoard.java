package com.elster.jupiter.rest.whiteboard.impl;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.*;
import org.glassfish.jersey.server.filter.*;
import org.glassfish.jersey.servlet.*;
import javax.ws.rs.container.*;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;

import java.util.*;

public class WhiteBoard {
	
	private final HttpContext httpContext;
	private final HttpService httpService;
    private volatile ServiceTracker<Application,Application> tracker;

    public WhiteBoard(HttpService httpService) {
        this.httpContext = new HttpContextImpl();
        this.httpService = httpService;
    }

    void open(BundleContext bundleContext) {
    	tracker = new ServiceTracker<>(bundleContext, Application.class, new ApplicationTrackerCustomizer(bundleContext));
    	tracker.open();
    }
    
    void close() {
    	tracker.close();
    }
    
    void addResource(Application application, String alias) {    	    	
        ResourceConfig secureConfig = ResourceConfig.forApplication(application);
        secureConfig.register(JacksonFeature.class);
        secureConfig.register(RoleFilter.class);
        secureConfig.register(RolesAllowedDynamicFeature.class);
        try {
        	ServletContainer container = new ServletContainer(secureConfig);
        	HttpServlet wrapper = new EventServletWrapper(new ServletWrapper(container));
        	httpService.registerServlet(getAlias(alias), wrapper, null, httpContext);
        } catch (ServletException | NamespaceException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void removeResource(Application application,String alias) {
        httpService.unregister(getAlias(alias));
    }
    
    private String getAlias(String alias) {
    	return "/api" + alias;
    }

    private class ApplicationTrackerCustomizer implements ServiceTrackerCustomizer<Application, Application>  {
    	private final BundleContext bundleContext;
    	
    	ApplicationTrackerCustomizer(BundleContext context) {
			this.bundleContext = context;
		}
    	
		@Override
		public Application addingService(ServiceReference<Application> reference) {
			Application application = bundleContext.getService(reference);
			addResource(application, (String) reference.getProperty("alias"));
			return application;
		}

		@Override
		public void modifiedService(ServiceReference<Application> reference, Application application) {						
		}

		@Override
		public void removedService(ServiceReference<Application> reference, Application application) {
			removeResource(application, (String) reference.getProperty("alias"));			
		}
    }
}
