package com.energyict.mdc.app;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.FileResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Activator implements BundleActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/multisense";

    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/js/mdc", new BundleResolver(bundleContext), new DefaultStartPage("Multi Sense App"));
//        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.energyict.mdc.app/src/main/web/js/mdc", new FileResolver(), new DefaultStartPage("Multi Sense App"));
        registration = bundleContext.registerService(HttpResource.class, resource, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

}
