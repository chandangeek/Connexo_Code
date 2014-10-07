package com.elster.jupiter.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/ext";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/ext";

    private volatile ServiceRegistration<HttpResource> registration;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
        registration = context.registerService(HttpResource.class, resource, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }


}