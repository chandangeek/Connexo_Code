package com.elster.jupiter.theme.skyline;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.FileResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Activator implements BundleActivator {

    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        String alias = "/skyline";

        HttpResource resource = new HttpResource(alias, "/js/skyline", new BundleResolver(bundleContext));
//        HttpResource resource = new HttpResource(alias, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.theme.skyline/src/main/web/js/skyline", new FileResolver());

        registration = bundleContext.registerService(HttpResource.class, resource, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

}