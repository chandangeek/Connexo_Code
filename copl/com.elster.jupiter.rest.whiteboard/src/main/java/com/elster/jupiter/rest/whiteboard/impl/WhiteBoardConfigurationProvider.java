package com.elster.jupiter.rest.whiteboard.impl;

import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;


/*
 * As Jersey does not publish OGSI services,
 * we need to use a bundle tracker to wait for Jersey OSGI initialization.
 *  
 *  This class also provides the configuration for the rest whiteboard
 *  
 */

@Component (name = "com.elster.jupiter.rest.whiteboard" , immediate = true , service = {}  )
public class WhiteBoardConfigurationProvider {
	private volatile BundleTracker<Bundle> tracker;
	private volatile ServiceRegistration<WhiteBoardConfigurationProvider> registration;
	private volatile WhiteBoardConfiguration configuration;
    
    @Activate
    public void activate(BundleContext context, Map<String,Object> properties) {  
    	this.configuration = WhiteBoardConfiguration.of(properties);
    	int stateMask = Bundle.ACTIVE | Bundle.START_TRANSIENT | Bundle.STARTING;
    	tracker = new BundleTracker<>(context, stateMask , new JerseyBundleTrackerCustomizer(context));
    	tracker.open();
    }
    
    @Deactivate
    public void deactivate() {
    	tracker.close();
    	if (registration != null) {
    		registration.unregister();
    	}
    }
    
    WhiteBoardConfiguration getConfiguration() {
    	return configuration;
    }
    
    void start(BundleContext bundleContext) {		
		registration = bundleContext.registerService(WhiteBoardConfigurationProvider.class,this,null);
	}
	
	private class JerseyBundleTrackerCustomizer implements BundleTrackerCustomizer<Bundle> {
		private final BundleContext bundleContext;
		
		JerseyBundleTrackerCustomizer(BundleContext context) {
			this.bundleContext = context; 
			// this activates the target bundle's classloader , to work around the bundle's lazy activation policy;
			Class<?> clazz = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.class;
			clazz.getAnnotations();
		}

		@Override
		public Bundle addingBundle(Bundle bundle, BundleEvent event) {
			if (! "org.glassfish.hk2.osgi-resource-locator".equals(bundle.getSymbolicName())) {
                return null;
            }
			if (bundle.getState() == Bundle.ACTIVE) {
				start(bundleContext);
				return null;
			} else {
				return bundle;
			}
		}

		@Override
		public void modifiedBundle(Bundle bundle, BundleEvent event, Bundle trackedBundle) {
			if (event.getType() == BundleEvent.STARTED) {
				start(bundleContext);
			}
		}

		@Override
		public void removedBundle(Bundle bundle, BundleEvent event, Bundle trackedBundle) {		
		}
		
	}	

}
