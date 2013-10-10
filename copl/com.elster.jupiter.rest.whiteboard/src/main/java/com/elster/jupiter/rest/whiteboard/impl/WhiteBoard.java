package com.elster.jupiter.rest.whiteboard.impl;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.*;
import org.glassfish.jersey.server.filter.*;
import org.glassfish.jersey.servlet.*;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.ws.rs.core.Application;

public class WhiteBoard {
	
	private volatile HttpContext httpContext;
	private final HttpService httpService;
    private volatile ServiceTracker<Application,Application> tracker;
    private volatile boolean debug;

    public WhiteBoard(HttpService httpService) {    
        this.httpService = httpService;
    }

    private Authentication createAuthentication(String method) {
    	System.out.println(method);
    	switch(method) {
    		case HttpServletRequest.DIGEST_AUTH:
    			return new DigestAuthentication();
    		default:
    			return new BasicAuthentication();    			    			
    	}
    }
    
    void open(BundleContext bundleContext,String authenticationMethod , boolean debug) {
    	this.httpContext = new HttpContextImpl(createAuthentication(authenticationMethod));     		
    	this.debug = debug;
    	tracker = new ServiceTracker<>(bundleContext, Application.class, new ApplicationTrackerCustomizer(bundleContext));
    	tracker.open();
    }
    
    void close() {
    	tracker.close();
    }
    
    void addResource(Application application, String alias) {    	    	
        ResourceConfig secureConfig = ResourceConfig.forApplication(application);
        secureConfig.register(ObjectMapperProvider.class);
        secureConfig.register(JacksonFeature.class);
        secureConfig.register(RoleFilter.class);
        secureConfig.register(RolesAllowedDynamicFeature.class);
        if (debug) {        	       
        	secureConfig.register(LoggingFilter.class);
        }
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
