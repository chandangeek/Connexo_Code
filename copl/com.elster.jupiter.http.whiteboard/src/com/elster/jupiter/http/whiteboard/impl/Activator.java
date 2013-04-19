package com.elster.jupiter.http.whiteboard.impl;

import org.osgi.framework.*;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.*;

public class Activator implements BundleActivator {

	private volatile BundleContext bundleContext;
	private volatile ServiceTracker<HttpService,HttpService> httpTracker;
	private volatile WhiteBoard whiteBoard;
	
	public void start(BundleContext bundleContext) throws Exception {
		this.bundleContext = bundleContext;
		httpTracker = new ServiceTracker<> (bundleContext, HttpService.class, new HttpCustomizer());
		httpTracker.open();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		httpTracker.close();
	}
	
	void startWhiteBoard(HttpService service) {
		whiteBoard = new WhiteBoard(bundleContext,service);
		whiteBoard.open();
	}
	
	void stopWhiteBoard(HttpService service) {
		whiteBoard.close();
		whiteBoard = null;
	}
	
	class HttpCustomizer implements ServiceTrackerCustomizer<HttpService, HttpService> {
		@Override
		public HttpService addingService(ServiceReference<HttpService> reference) {
			HttpService service = bundleContext.getService(reference);
			startWhiteBoard(service);
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
