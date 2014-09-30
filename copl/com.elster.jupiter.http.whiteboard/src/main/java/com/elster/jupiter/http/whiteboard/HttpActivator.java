package com.elster.jupiter.http.whiteboard;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public abstract class HttpActivator implements BundleActivator {

    private final String alias;
    private final String localName;
    private final Boolean isAbsoluteName;

    private volatile ServiceRegistration<HttpResource> registration;

    protected HttpActivator(String alias, String localName) {
        this(alias, localName, false);
    }

    protected HttpActivator(String alias, String localName, Boolean isAbsoluteName) {
        this.alias = alias;
        this.localName = localName;
        this.isAbsoluteName = isAbsoluteName;
    }

    public Resolver createResolver(BundleContext context) {
        if (isAbsoluteName) {
            return new FileResolver();
        }

        return new BundleResolver(context);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        HttpResource resource = new HttpResource(alias, localName, createResolver(context));
        registration = context.registerService(HttpResource.class, resource, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

}
