package com.elster.jupiter.rest.whiteboard;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.sun.jersey.api.container.filter.ResourceDebuggingFilterFactory;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.osgi.service.component.annotations.*;
import org.osgi.service.http.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import java.util.*;


@Component (name = "com.elster.jupiter.rest.whiteboard" , service = {})
public class WhiteBoard implements ServiceLocator {

	private final HttpContext httpContext;
    private volatile HttpService httpService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;


    public WhiteBoard() {
        this.httpContext = new HttpContextImpl();            
    }

    @Reference(name = "ZResource" , cardinality=ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addResource(Application application, Map<String,Object> properties) {
    	String alias = (String) properties.get("alias");
        ResourceConfig secureConfig = new ApplicationAdapter(application);
        List<Class<? extends ContainerRequestFilter>> requestFilters = new ArrayList<>();
        requestFilters.add(RoleFilter.class);
        secureConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, requestFilters);
        List<Class<? extends ResourceFilterFactory>> resourceFilterFactories = new ArrayList<>();
        resourceFilterFactories.add(RolesAllowedResourceFilterFactory.class);
        resourceFilterFactories.add(ResourceDebuggingFilterFactory.class);
        secureConfig.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, resourceFilterFactories);
        ServletContainer container = new ServletContainer(secureConfig);
        HttpServlet wrapper = new ServletWrapper(container);
        try {
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
	
}
