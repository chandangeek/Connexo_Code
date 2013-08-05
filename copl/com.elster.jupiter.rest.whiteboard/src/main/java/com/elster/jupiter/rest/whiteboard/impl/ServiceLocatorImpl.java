package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.http.*;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.util.*;


@Component (name = Bus.PID , immediate = true , service = {}  )
public class ServiceLocatorImpl implements  ServiceLocator {
	
	private static final String DEBUG = "debug";
	
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile boolean debug;
    private volatile BundleTracker<Bundle> tracker;
    private volatile WhiteBoard whiteBoard;
    private volatile Publisher publisher;

    public ServiceLocatorImpl() {            
    }

    @Reference
    public void setHttpService(HttpService httpService) {
    	this.whiteBoard = new WhiteBoard(httpService);
    }
    
    @Activate
    public void activate(BundleContext context, Map<String,Object> properties) {    	
    	Bus.setServiceLocator(this);
    	configure(properties);
    	int stateMask = Bundle.ACTIVE | Bundle.START_TRANSIENT | Bundle.STARTING;
    	tracker = new BundleTracker<>(context, stateMask , new JerseyBundleTrackerCustomizer(context));
    	tracker.open();
    }
    
  
    public void configure(Map<String,Object> properties)  {
    	debug = false;
		if (properties != null) {
			Object value = properties.get(DEBUG);
			if (value != null && value instanceof Boolean) { 
				debug = (Boolean) value;
			}
		}				    	    	
	}
    
    @Deactivate
    public void deactivate() {
    	tracker.close();    	
    	whiteBoard.close();    	
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

	public void configure(Dictionary<String, ? > dict)  {	
		if (dict == null) {
			debug = false;
		} else {
			debug = (Boolean) dict.get(DEBUG);
		}				
    	tracker.open();    	
	}
	
	void startWhiteBoard(BundleContext bundleContext) {		
		whiteBoard.open(bundleContext);
	}
	
	public boolean getDebug() {
		return debug;
	}

    @Override
    public Publisher getPublisher() {
        return publisher;
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    private class JerseyBundleTrackerCustomizer implements BundleTrackerCustomizer<Bundle> {
		private final BundleContext bundleContext;
		
		JerseyBundleTrackerCustomizer(BundleContext context) {
			this.bundleContext = context;
		}

		@Override
		public Bundle addingBundle(Bundle bundle, BundleEvent event) {
			if (!"com.sun.jersey.core".equals(bundle.getSymbolicName())) {
                return null;
            }
			if (bundle.getState() == Bundle.ACTIVE) {
				startWhiteBoard(bundleContext);
				return null;
			} else {
				return bundle;
			}
		}

		@Override
		public void modifiedBundle(Bundle bundle, BundleEvent event, Bundle trackedBundle) {
			if (event.getType() == BundleEvent.STARTED) {
				startWhiteBoard(bundleContext);
			}
		}

		@Override
		public void removedBundle(Bundle bundle, BundleEvent event, Bundle trackedBundle) {		
		}
		
	}
}
