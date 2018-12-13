/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.rest.ServletWrapper;

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

	@Reference(name = "ZInboundSoapEndPointProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addEndPoint(InboundSoapEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias == null) {
    		return;
    	}
		webServicesService.register(alias, provider);
	}

	public void removeEndPoint(InboundSoapEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias != null) {
			webServicesService.unregister(alias);
		}
	}

	@Reference(name = "ZInboundRestEndPointProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addEndPoint(InboundRestEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias == null) {
			return;
		}
		webServicesService.register(alias, provider);
	}

	public void removeEndPoint(InboundRestEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias != null) {
			webServicesService.unregister(alias);
		}
	}

	@Reference(name = "ZOutboundSoapEndPointProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addEndPoint(OutboundSoapEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias == null) {
			return;
		}
		webServicesService.register(alias, provider);
	}

	public void removeEndPoint(OutboundSoapEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias != null) {
			webServicesService.unregister(alias);
		}
	}

	@Reference(name = "ZOutboundRestEndPointProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addEndPoint(OutboundRestEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias == null) {
			return;
		}
		webServicesService.register(alias, provider);
	}

	public void removeEndPoint(OutboundRestEndPointProvider provider, Map<String, Object> props) {
		String alias = getName(props);
		if (alias != null) {
			webServicesService.unregister(alias);
		}
	}


	private String getName(Map<String, Object> props) {
		return props == null ? null : (String) props.get("name");
	}
   
}
