/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.util.osgi.BundleWaiter;

import org.apache.cxf.transport.http.HTTPConduitConfigurer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;


/*
 * As Apache CXF core does not publish OGSI services,
 * we need to use a bundle tracker to wait for Jersey OSGI initialization.
 *  
 *  This class also provides the configuration for the rest whiteboard
 *  
 */

@Component (name = "com.elster.jupiter.soap.whiteboard" , immediate = true , service = {}  )
public class WhiteBoardConfigurationProvider implements BundleWaiter.Startable {
	private volatile ServiceRegistration<WhiteBoardConfigurationProvider> registration;
	private volatile WhiteBoardConfiguration configuration;
    
	@Reference
	public void setHttpConduitConfigurer(HTTPConduitConfigurer configurer) {
		// wait for initialization of CXF HTTP
	}
	
    @Activate
    public void activate(BundleContext context, Map<String,Object> properties) {  
    	this.configuration = WhiteBoardConfiguration.of(properties);
		BundleWaiter.wait(this, context, "org.apache.cxf.cxf-core");
	}
    
    @Deactivate
    public void deactivate() {
    	if (registration != null) {
    		registration.unregister();
    	}
    }
    
    WhiteBoardConfiguration getConfiguration() {
    	return configuration;
    }
    
    public void start(BundleContext bundleContext) {		
		registration = bundleContext.registerService(WhiteBoardConfigurationProvider.class,this,null);
	}
	
}
