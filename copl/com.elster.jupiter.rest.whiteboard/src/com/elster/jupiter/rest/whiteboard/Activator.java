package com.elster.jupiter.rest.whiteboard;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;
import org.osgi.service.http.*;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

public class Activator implements BundleActivator, ServiceLocator {
	
	private volatile BundleContext bundleContext;
	private volatile ServiceTracker<HttpService,HttpService> httpTracker;
	private volatile ServiceTracker<UserService,UserService> userTracker;
	private volatile ServiceTracker<ThreadPrincipalService,ThreadPrincipalService> principalTracker;
	private volatile WhiteBoard whiteBoard;
	
	public void start(BundleContext bundleContext) throws Exception {
		this.bundleContext = bundleContext;
		httpTracker = new ServiceTracker<> (bundleContext, HttpService.class, new HttpCustomizer());
		httpTracker.open();
		userTracker = new ServiceTracker<> (bundleContext, UserService.class, null);
		userTracker.open();
		principalTracker = new ServiceTracker<> (bundleContext,ThreadPrincipalService.class,null);
		principalTracker.open();
		Bus.setServiceLocator(this);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		httpTracker.close();
		userTracker.close();
		principalTracker.close();
		Bus.setServiceLocator(null);
	}
	
	void startWhiteBoard(final HttpService service , final long delay) {
		whiteBoard = new WhiteBoard(bundleContext,service);
		whiteBoard.open(delay);
	}
	
	void stopWhiteBoard(HttpService service) {
		whiteBoard.close();
		whiteBoard = null;
	}
	
	@Override
	public UserService getUserService() {
		return userTracker.getService();
	}
	
	@Override
	public ThreadPrincipalService getThreadPrincipalService() {
		return principalTracker.getService();
	}
	
	class HttpCustomizer implements ServiceTrackerCustomizer<HttpService, HttpService> {
		@Override
		public HttpService addingService(ServiceReference<HttpService> reference) {
			HttpService service = bundleContext.getService(reference);
			startWhiteBoard(service,1000L);
			return service;
		}

		@Override
		public void modifiedService(ServiceReference<HttpService> reference,HttpService service) {
		}

		@Override
		public void removedService(ServiceReference<HttpService> reference,HttpService service) {
			stopWhiteBoard(service);			
		}
	}	
	
}
