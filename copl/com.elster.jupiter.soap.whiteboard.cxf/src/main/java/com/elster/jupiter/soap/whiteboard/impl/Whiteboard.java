package com.elster.jupiter.soap.whiteboard.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.ws.Endpoint;

import org.apache.cxf.jaxws.spi.ProviderImpl;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.elster.jupiter.soap.whiteboard.EndPointProvider;

@Component(name = "com.elster.jupiter.soap.whiteboard.implementation", service = {} , immediate = true)
public class Whiteboard {
	private final Map<EndPointProvider,Endpoint> endpoints = new ConcurrentHashMap<>();
	
	@Reference
    public void setHttpService(HttpService httpService) {
    	HttpServlet servlet = new ServletWrapper(new CXFNonSpringServlet()); 
    	try {
    		httpService.registerServlet("/soap",servlet,null,null);
    	} catch (NamespaceException | ServletException ex) {
    		throw new RuntimeException(ex);
    	}
    }
	
	@Reference
	public void setConfiguration(WhiteBoardConfigurationProvider provider) {		
	}
	 
    @Reference(name="ZEndPointProvider",cardinality=ReferenceCardinality.MULTIPLE,policy=ReferencePolicy.DYNAMIC)
    public void addEndPoint(EndPointProvider provider,Map<String,Object> props) {
    	String alias = getAlias(props);
    	if (alias == null) {
    		return;
    	}
    	System.out.println("Serving " + alias);
    	// trick javax.xml.ws to find cxf provider
    	ClassLoader old = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(ProviderImpl.class.getClassLoader());
    		Endpoint endpoint = Endpoint.publish(alias, Objects.requireNonNull(provider.get()));
    		endpoints.put(provider,endpoint);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		Thread.currentThread().setContextClassLoader(old);
    	}
    }

    public void removeEndPoint(EndPointProvider provider) {
    	Endpoint endPoint = endpoints.remove(provider);
    	if (endPoint != null) {
    		endPoint.stop();
    	}
    }
    
    private String getAlias(Map<String,Object> props) {
    	return props == null ? null : (String) props.get("alias");
    }
   
}
