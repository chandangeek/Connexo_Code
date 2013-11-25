package com.elster.jupiter.rest.whiteboard.impl;

import com.google.common.base.Strings;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class WhiteBoard {
	
	private volatile HttpContext httpContext;
	private final HttpService httpService;
    private volatile ServiceTracker<Application,Application> tracker;
    private volatile boolean debug;

    public WhiteBoard(HttpService httpService) {    
        this.httpService = httpService;
    }

    private Authentication createAuthentication(String method) {
    	switch(Strings.nullToEmpty(method)) {
    		case HttpServletRequest.DIGEST_AUTH:
    			return new DigestAuthentication();
    		// case fall through to document default 
    		case HttpServletRequest.BASIC_AUTH:
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
        for (Object object : application.getSingletons()) {
            if (object.getClass().isAnnotationPresent(Provider.class)) {
                System.err.println("Registered "+object);
                secureConfig.register(object);
            }
        }

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
            try {
                Application application = bundleContext.getService(reference);
                addResource(application, (String) reference.getProperty("alias"));
                return application;
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to start service "+reference.getProperty("alias"));
                e.printStackTrace();
                throw e;
            }
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
