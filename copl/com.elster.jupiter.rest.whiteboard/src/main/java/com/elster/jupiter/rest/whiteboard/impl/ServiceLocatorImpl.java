package com.elster.jupiter.rest.whiteboard.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.rest.whiteboard.RestCallExecutedEvent;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;


/*
 * As Jersey does not publish OGSI services,
 * we need to use a bundle tracker to wait for Jersey OSG initialization.
 *  
 */

@Component (name = Bus.PID , immediate = true , service = {}  )
public class ServiceLocatorImpl implements  ServiceLocator {
	
	private static final String DEBUG = "debug";
	private static final String LOG = "log";
	private static final String EVENT = "event";
	private static final String AUTHENTICATION = "authentication";
	
	private volatile boolean debug;
	private volatile boolean log;
	private volatile boolean throwEvents;
	private volatile Optional<String> authenticationMethod;
	
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private AtomicReference<EventAdmin> eventAdminServiceHolder = new AtomicReference<>();
    
    private volatile BundleTracker<Bundle> tracker;
    private volatile Publisher publisher;

    public ServiceLocatorImpl() {            
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
    	if (properties == null) {
    		debug = false;
    		log = false;
    		throwEvents = false;	
    		authenticationMethod = Optional.absent();
    	} else {
    		debug = Boolean.TRUE.equals(properties.get(DEBUG));
			log = Boolean.TRUE.equals(properties.get(LOG));
			throwEvents = Boolean.TRUE.equals(properties.get(EVENT));
			authenticationMethod = Optional.fromNullable((String) properties.get(AUTHENTICATION));			
		}				    	    	
	}
    
    @Deactivate
    public void deactivate() {
    	tracker.close();    	  	
    	Bus.clearServiceLocator(this);
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

	
	void startWhiteBoard(BundleContext bundleContext) {		
		bundleContext.registerService(ServiceLocator.class,this,null);
		//whiteBoard.open(bundleContext,authenticationMethod.orNull(),debug);
	}
		
    @Override
    public Publisher getPublisher() {
        return publisher;
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }
    
    @Reference(cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC)
    public void setEventAdmin(EventAdmin eventAdmin) {
    	eventAdminServiceHolder.set(eventAdmin);
    }
    
    public void unsetEventAdmin(EventAdmin eventAdmin) {
    	eventAdminServiceHolder.compareAndSet(eventAdmin, null);
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

	@Override
	public void fire(RestCallExecutedEvent event) {
		publisher.publish(event);
		if (log) {
			Logger.getLogger("com.elster.jupiter.rest.whiteboard").info(event.toString());
		}
		if (throwEvents) {			
			EventAdmin eventAdmin = eventAdminServiceHolder.get();
			if (eventAdmin != null) {
				eventAdmin.postEvent(event.toOsgiEvent());
			}
		}
	}

	@Override
	public String getAuthenticationMethhod() {
		return authenticationMethod.orNull();
	}

	@Override
	public boolean debug() {
		return debug;
	}
}
