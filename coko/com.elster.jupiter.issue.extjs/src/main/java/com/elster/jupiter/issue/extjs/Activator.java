package com.elster.jupiter.issue.extjs;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.elster.jupiter.http.whiteboard.*;


public class Activator implements BundleActivator {

	private volatile ServiceRegistration<HttpResource> registration;
	
	public void start(BundleContext bundleContext) throws Exception {
		String alias = "/issue";
		HttpResource resource = new HttpResource(alias, "/js/issue" , new BundleResolver(bundleContext), new DefaultStartPage("index.html"));
		registration = bundleContext.registerService(HttpResource.class, resource , null);
	}
	
	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}

}
