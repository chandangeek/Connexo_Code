/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.login.app;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/login";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/login";

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