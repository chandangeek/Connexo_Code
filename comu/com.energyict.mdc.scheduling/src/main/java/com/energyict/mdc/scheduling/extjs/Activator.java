package com.energyict.mdc.scheduling.extjs;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.elster.jupiter.http.whiteboard.*;

import java.util.Arrays;


public class Activator implements BundleActivator {

    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        String alias = "/dcs";
        DefaultStartPage dcs = new DefaultStartPage("Dcs", "", "/index.html", "Dcs.controller.Main",null, Arrays.asList("DCS"));
        HttpResource resource = new HttpResource(alias, "C:/jupiterrepo/com.energyict.mdc.scheduling.extjs/src/main/web/js/dcs" , new FileResolver(), dcs);
        registration = bundleContext.registerService(HttpResource.class, resource , null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

}
