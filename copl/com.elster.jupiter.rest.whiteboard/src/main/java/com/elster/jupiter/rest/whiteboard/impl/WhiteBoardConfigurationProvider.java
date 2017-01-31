/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.elster.jupiter.util.osgi.BundleWaiter;


/*
 * As Jersey does not publish OGSI services,
 * we need to use a bundle tracker to wait for Jersey OSGI initialization.
 *  
 *  This class also provides the configuration for the rest whiteboard
 *  
 */

@Component (name = "com.elster.jupiter.rest.whiteboard" , immediate = true , service = {}  )
public class WhiteBoardConfigurationProvider implements BundleWaiter.Startable {
	private volatile ServiceRegistration<WhiteBoardConfigurationProvider> registration;
	private volatile WhiteBoardConfiguration configuration;
    
    @Activate
    public void activate(BundleContext context, Map<String,Object> properties) {  
    	this.configuration = WhiteBoardConfiguration.of(properties);
    	// work around lazy activation policy
    	Class<?> clazz = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.class;
		clazz.getAnnotations();
    	BundleWaiter.wait(this,context,"org.glassfish.hk2.osgi-resource-locator");
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
