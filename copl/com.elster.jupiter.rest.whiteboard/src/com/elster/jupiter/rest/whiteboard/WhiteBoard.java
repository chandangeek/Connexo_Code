package com.elster.jupiter.rest.whiteboard;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.container.filter.ResourceDebuggingFilterFactory;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.*;
import org.osgi.service.http.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;

import java.util.*;


@Component (name = Bus.PID , immediate = true , service = {ManagedService.class} , property = { Constants.SERVICE_PID + "=" + Bus.PID} )
public class WhiteBoard implements ManagedService , ServiceLocator {
	
	private static final String DEBUG = "debug";
	
	private final HttpContext httpContext;
    private volatile HttpService httpService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile boolean debug;

    public WhiteBoard() {
        this.httpContext = new HttpContextImpl();            
    }

    @Reference(name = "ZResource" , cardinality=ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addResource(final Application application, final Map<String,Object> properties) {
    	try {
    		doAddResource(application, properties);
    	} catch (ContainerException ex) {
    		// caused by a race condition in Jersey start up code, retry
    		System.out.println("Race condition");
    		Runnable runnable = new Runnable() {				
				@Override
				public void run() {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException ex) {						
					}
					//protect against service deactivation
					if (httpService != null) {
						doAddResource(application, properties);
					}
				}
			};
			new Thread(runnable).start();
    	}
    }
    
    private void doAddResource(Application application, Map<String,Object> properties) {
    	String alias = (String) properties.get("alias");
    	System.out.println("Adding " + alias);
        ResourceConfig secureConfig = new ApplicationAdapter(application);
        List<Class<? extends ContainerRequestFilter>> requestFilters = new ArrayList<>();
        requestFilters.add(RoleFilter.class);
        secureConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, requestFilters);
        List<Class<? extends ResourceFilterFactory>> resourceFilterFactories = new ArrayList<>();
        resourceFilterFactories.add(RolesAllowedResourceFilterFactory.class);
        if (debug) {
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

    void removeResource(Application application,Map<String,Object> properties) {
        httpService.unregister((String) properties.get("alias"));
    }

    @Reference
    public void setHttpService(HttpService httpService) {
    	this.httpService = httpService;
    }
    
    @Activate
    public void activate() {    	
    	Bus.setServiceLocator(this);    	
    }
    
    @Deactivate
    public void deActivate() {    
    	this.httpService = null;
    	Bus.setServiceLocator(null);    	 	
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

	@Override
	public void updated(Dictionary<String, ? > dict)  {	
		if (dict == null) {
			debug = false;
		} else {
			debug = (Boolean) dict.get(DEBUG);
		}
	}
	
}
