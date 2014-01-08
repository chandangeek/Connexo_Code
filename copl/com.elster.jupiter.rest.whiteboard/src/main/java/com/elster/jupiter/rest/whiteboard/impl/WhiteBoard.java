package com.elster.jupiter.rest.whiteboard.impl;

import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.elster.jupiter.rest.util.BinderProvider;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

@Component (name = "com.elster.jupiter.rest.whiteboard2" , immediate = true , service = {}  )
public class WhiteBoard {
	
	private volatile HttpContext httpContext;
	private volatile HttpService httpService;
	private volatile ServiceLocator serviceLocator;
    private volatile boolean debug;

    public WhiteBoard() {    
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
    }
   
    @Reference(name="A_HttpService")
    public void setHttpService(HttpService httpService) {
    	this.httpService = httpService;
    }
    
    @Reference(name="B_ServiceLocator")
    public void setServiceLocator(ServiceLocator serviceLocator) {
    	this.serviceLocator = serviceLocator;
    	this.httpContext = new HttpContextImpl(createAuthentication(serviceLocator.getAuthenticationMethhod()));     		
    	this.debug = this.serviceLocator.debug();
    }
    
    @Reference(name="Z_Application",cardinality=ReferenceCardinality.MULTIPLE,policy=ReferencePolicy.DYNAMIC)
    public void addResource(Application application, Map<String,Object> properties) {
    	Optional<String> alias = getAlias(properties);
    	if (!alias.isPresent()) {
    		return;
    	}
        ResourceConfig secureConfig = ResourceConfig.forApplication(Objects.requireNonNull(application));
        secureConfig.register(ObjectMapperProvider.class);
        secureConfig.register(JacksonFeature.class);
        secureConfig.register(RoleFilter.class);
        secureConfig.register(RolesAllowedDynamicFeature.class);
        if (application instanceof BinderProvider) {
        	secureConfig.register(((BinderProvider) application).getBinder());
        }
        if (debug) {        	       
        	secureConfig.register(LoggingFilter.class);
        }
        try {
        	ServletContainer container = new ServletContainer(secureConfig);
        	HttpServlet wrapper = new EventServletWrapper(new ServletWrapper(container));
        	httpService.registerServlet(alias.get(), wrapper, null, httpContext);
        } catch (ServletException | NamespaceException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void removeResource(Application application,Map<String,Object> properties) {
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
