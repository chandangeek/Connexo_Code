package com.elster.jupiter.rest.whiteboard;

import com.sun.jersey.api.container.filter.ResourceDebuggingFilterFactory;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import com.sun.jersey.spi.container.servlet.ServletContainer;

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
        ResourceConfig secureConfig = new ApplicationAdapter(application);
        List<Class<? extends ContainerRequestFilter>> requestFilters = new ArrayList<>();
        requestFilters.add(RoleFilter.class);
        secureConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, requestFilters);
        List<Class<? extends ResourceFilterFactory>> resourceFilterFactories = new ArrayList<>();
        resourceFilterFactories.add(RolesAllowedResourceFilterFactory.class);
        if (Bus.getServiceLocator().getDebug()) {
        	resourceFilterFactories.add(ResourceDebuggingFilterFactory.class);
        }
        secureConfig.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, resourceFilterFactories);
        try {
        	ServletContainer container = new ServletContainer(secureConfig);
        	HttpServlet wrapper = new ServletWrapper(container);
        	httpService.registerServlet(alias, wrapper, null, httpContext);
        } catch (ServletException | NamespaceException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void removeResource(Application application,String alias) {
        httpService.unregister(alias);
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
		public void modifiedService(ServiceReference<Application> refernce, Application application) {						
		}

		@Override
		public void removedService(ServiceReference<Application> reference, Application application) {
			removeResource(application, (String) reference.getProperty("alias"));			
		}
    }
}
