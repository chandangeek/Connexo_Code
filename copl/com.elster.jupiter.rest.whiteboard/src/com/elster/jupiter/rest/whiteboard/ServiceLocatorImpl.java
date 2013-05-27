package com.elster.jupiter.rest.whiteboard;

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


@Component (name = Bus.PID , immediate = true , service = {ManagedService.class} , property = { Constants.SERVICE_PID + "=" + Bus.PID} )
public class ServiceLocatorImpl implements ManagedService , ServiceLocator {
	
	private static final String DEBUG = "debug";
	
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile boolean debug;
    private volatile BundleTracker<Bundle> tracker;
    private volatile WhiteBoard whiteBoard;

    public ServiceLocatorImpl() {            
    }

    @Reference
    public void setHttpService(HttpService httpService) {
    	this.whiteBoard = new WhiteBoard(httpService);
    }
    
    public void activate(ComponentContext context) {
    	Bus.setServiceLocator(this);  
    	int stateMask = Bundle.ACTIVE | Bundle.START_TRANSIENT | Bundle.STARTING;
    	tracker = new BundleTracker<>(context.getBundleContext(), stateMask , new JerseyBundleTrackerCustomizer(context.getBundleContext()));
    	tracker.open();    	
    }
    
    public void deActivate(ComponentContext context) {
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

	@Override
	public void updated(Dictionary<String, ? > dict)  {	
		if (dict == null) {
			debug = false;
		} else {
			debug = (Boolean) dict.get(DEBUG);
		}		
	}
	
	void startWhiteBoard(BundleContext bundleContext) {		
		whiteBoard.open(bundleContext);
	}
	
	public boolean getDebug() {
		return debug;
	}
	
	private class JerseyBundleTrackerCustomizer implements BundleTrackerCustomizer<Bundle> {
		private final BundleContext bundleContext;
		
		JerseyBundleTrackerCustomizer(BundleContext context) {
			this.bundleContext = context;
		}

		@Override
		public Bundle addingBundle(Bundle bundle, BundleEvent event) {
			if (!bundle.getSymbolicName().equals("com.sun.jersey.core"))
				return null;
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
			System.out.println("Removing bundle " + bundle.getSymbolicName() + " event " + event);
		}
		
	}
}
