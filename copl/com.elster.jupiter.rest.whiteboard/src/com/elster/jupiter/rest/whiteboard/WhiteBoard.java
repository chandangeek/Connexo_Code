package com.elster.jupiter.rest.whiteboard;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.sun.jersey.api.container.filter.ResourceDebuggingFilterFactory;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.List;

@Component (name = "com.elster.jupiter.rest.whiteboard" , service = {})
public class WhiteBoard implements ServiceLocator {

    private volatile HttpService httpService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private final HttpContext httpContext;
    private volatile ServiceTracker<Application, Application> resourceTracker;

    public WhiteBoard() {
        this.httpContext = new HttpContextImpl();        
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
        secureConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, requestFilters);
        List<Class<? extends ResourceFilterFactory>> resourceFilterFactories = new ArrayList<>();
        resourceFilterFactories.add(RolesAllowedResourceFilterFactory.class);
        resourceFilterFactories.add(ResourceDebuggingFilterFactory.class);
        secureConfig.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, resourceFilterFactories);
        com.sun.jersey.spi.container.servlet.ServletContainer container = new com.sun.jersey.spi.container.servlet.ServletContainer(secureConfig);
        HttpServlet wrapper = new ServletWrapper(container);
        try {
            httpService.registerServlet(alias, wrapper, null, httpContext);
        } catch (ServletException | NamespaceException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void removeResource(String alias) {
        httpService.unregister(alias);
    }

    @Reference
    public void setHttpService(HttpService httpService) {
    	this.httpService = httpService;
    }
    
    @Activate
    public void activate(BundleContext context) {    	
    	resourceTracker = new ServiceTracker<>(context, Application.class,  new ApplicationTrackerCustomizer(context));
    	Bus.setServiceLocator(this);
    	open(1000L);
    }
    
    @Deactivate
    public void deActivate() {
    	Bus.setServiceLocator(null);
    	resourceTracker.close();
    	
    }
    @Override
	public UserService getUserService() {
		return userService;
	}

	@Override
	public ThreadPrincipalService getThreadPrincipalService() {
		return threadPrincipalService;
	}

	@Reference
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Reference
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;
	}
	
    private class ApplicationTrackerCustomizer implements ServiceTrackerCustomizer<Application, Application> {
    	private final BundleContext bundleContext;
    	
    	ApplicationTrackerCustomizer(BundleContext context) {
    		this.bundleContext = context;
		}

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
                addResource(alias, application);
                return application;
            }
        }

        @Override
        public void modifiedService(ServiceReference<Application> reference, Application service) {
        }

        @Override
        public void removedService(ServiceReference<Application> reference, Application service) {
            String alias = getAlias(reference);
            if (alias != null) {
                removeResource(alias);
            }
        }


    }
    
}
