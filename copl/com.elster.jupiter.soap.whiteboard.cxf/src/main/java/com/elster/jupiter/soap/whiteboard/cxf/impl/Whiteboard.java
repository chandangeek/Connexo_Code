package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Map;

@Component(name = "com.elster.jupiter.soap.whiteboard.implementation", service = {} , immediate = true)
public class Whiteboard {
	private WebServicesServiceImpl webServicesService;

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

	@Reference
	public void setWebServicesService(WebServicesService webServicesService) {
		this.webServicesService = (WebServicesServiceImpl) webServicesService;
	}
	
    @Reference(name="ZEndPointProvider",cardinality=ReferenceCardinality.MULTIPLE,policy=ReferencePolicy.DYNAMIC)
	public void addEndPoint(InboundEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias == null) {
    		return;
    	}
		webServicesService.register(alias, provider);
	}

	public void removeEndPoint(EndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias != null) {
			webServicesService.unregister(alias);
		}
	}

	@Reference(name = "ZOutboundEndPointProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addEndPoint(OutboundEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias == null) {
			return;
		}
		webServicesService.register(alias, provider);
	}

	public void removeEndPoint(OutboundEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias != null) {
			webServicesService.unregister(alias);
		}
	}

	private String getName(Map<String, Object> props) {
		return props == null ? null : (String) props.get("name");
	}
   
}
