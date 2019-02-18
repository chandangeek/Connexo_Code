/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.loaderjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/ldr";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/ldr";

    private volatile ServiceRegistration<HttpResource> registration;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
        // EXAMPLE: Below is how to enable local development mode.
//      HttpResource resource =  new HttpResource(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.loaderjs/src/main/web/js/ldr", new FileResolver());
        registration = context.registerService(HttpResource.class, resource, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

}