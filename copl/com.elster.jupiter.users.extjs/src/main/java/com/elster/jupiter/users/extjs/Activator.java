package com.elster.jupiter.users.extjs;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.elster.jupiter.http.whiteboard.*;

import java.util.Arrays;


public class Activator implements BundleActivator {

	private volatile ServiceRegistration<HttpResource> registration;
	
	public void start(BundleContext bundleContext) throws Exception {
		String alias = "/usr";
		DefaultStartPage usr = new DefaultStartPage("Usr", "", "/index.html", "Usr.controller.Main",null, Arrays.asList("USM"));
        HttpResource resource = new HttpResource(alias, "/js/usr" , new BundleResolver(bundleContext), usr);
        registration = bundleContext.registerService(HttpResource.class, resource , null);
	}
	
	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}

}
