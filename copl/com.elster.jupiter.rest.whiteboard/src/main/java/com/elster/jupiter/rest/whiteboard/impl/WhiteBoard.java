package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.whiteboard.RestCallExecutedEvent;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component (name = "com.elster.jupiter.rest.whiteboard.implementation" , immediate = true , service = {}  )
public class WhiteBoard {
	
	private static final Logger LOGGER = Logger.getLogger(WhiteBoard.class.getName());
	
	private volatile HttpContext httpContext;
	private volatile HttpService httpService;
	private volatile UserService userService;
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile Publisher publisher;
	private AtomicReference<EventAdmin> eventAdminHolder = new AtomicReference<>();
	private volatile WhiteBoardConfiguration configuration;

    private Authentication createAuthentication(String method) {
    	switch(Strings.nullToEmpty(method)) {
    		case HttpServletRequest.DIGEST_AUTH:
    			return new DigestAuthentication(userService);
    		// case fall through to document default 
    		case HttpServletRequest.BASIC_AUTH:
    		default:
    			return new BasicAuthentication(userService);    			    			
    	}
    }
    
	UserService getUserService() {
		return userService;
	}


	ThreadPrincipalService getThreadPrincipalService() {
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

	@Reference
    public void setHttpService(HttpService httpService) {
    	this.httpService = httpService;
    }
	
	@Reference
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}
	
	Publisher getPublisher() {
		return publisher;
	}
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC)
	public void setEventAdmin(EventAdmin eventAdmin) {
		eventAdminHolder.set(eventAdmin);
	}
	    
	public void unsetEventAdmin(EventAdmin eventAdmin) {
	    eventAdminHolder.compareAndSet(eventAdmin, null);
	}
    
    @Reference(name="YServiceLocator")
    public void setConfiguration(WhiteBoardConfigurationProvider provider) {
    	this.configuration = provider.getConfiguration();
    	this.httpContext = new HttpContextImpl(createAuthentication(configuration.authenticationMethod()));     		
    }
    
	void fire(RestCallExecutedEvent event) {
		publisher.publish(event);
		if (configuration.log()) {
			Logger.getLogger("com.elster.jupiter.rest.whiteboard").info(event.toString());
		}
		if (configuration.throwEvents()) {			
			EventAdmin eventAdmin = eventAdminHolder.get();
			if (eventAdmin != null) {
				eventAdmin.postEvent(event.toOsgiEvent());
			}
		}
	}
    
    @Reference(name="ZApplication",cardinality=ReferenceCardinality.MULTIPLE,policy=ReferencePolicy.DYNAMIC)
    public void addResource(Application application, Map<String,Object> properties) {
    	Optional<String> alias = getAlias(properties);
    	if (!alias.isPresent()) {
    		return;
    	}
        ResourceConfig secureConfig = ResourceConfig.forApplication(Objects.requireNonNull(application));
        secureConfig.register(ObjectMapperProvider.class);
        secureConfig.register(JacksonFeature.class);
        secureConfig.register(new RoleFilter(threadPrincipalService));
        secureConfig.register(RolesAllowedDynamicFeature.class);
        if (application instanceof BinderProvider) {
        	secureConfig.register(((BinderProvider) application).getBinder());
        }
        if (configuration.debug()) {        	       
        	secureConfig.register(LoggingFilter.class);
        }
        try (ContextClassLoaderResource ctx = ContextClassLoaderResource.of(application.getClass())) {
            ServletContainer container = new ServletContainer(secureConfig);
        	HttpServlet wrapper = new EventServletWrapper(new ServletWrapper(container,threadPrincipalService),this);
        	httpService.registerServlet(alias.get(), wrapper, null, httpContext);
        } catch (ServletException | NamespaceException e) {
        	LOGGER.log(Level.SEVERE, "Error while registering " + alias.get() + ": " + e.getMessage() , e);
            throw new RuntimeException(e);
        } 
    }


    public void removeResource(Application application,Map<String,Object> properties) {
    	Optional<String> alias = getAlias(properties);
    	if (alias.isPresent()) {
    		httpService.unregister(alias.get());
    	}
    }
    
    private Optional<String> getAlias(Map<String,Object> properties) {
    	String alias = (String) properties.get("alias");
    	return alias == null ? Optional.<String>absent() : Optional.of("/api" + alias);
    }

 
}
