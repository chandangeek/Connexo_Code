package com.energyict.mdc.device.data.imp.extjs;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.elster.jupiter.http.whiteboard.*;

import java.util.Arrays;


public class Activator implements BundleActivator {

    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        String alias = "/bpm";
        DefaultStartPage bpm = new DefaultStartPage("Bpm", "", "/index.html", "Bpm.controller.Main",null, Arrays.asList("BPM"));
        HttpResource resource = new HttpResource(alias, "/js/bpm" , new BundleResolver(bundleContext), bpm);
        registration = bundleContext.registerService(HttpResource.class, resource , null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

}
