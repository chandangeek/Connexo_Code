package com.energyict.mdc.issue.datacollection.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.FileResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Arrays;

public class Activator {

    public static final String HTTP_RESOURCE_ALIAS = "/idc";

    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        DefaultStartPage idc = new DefaultStartPage("Idc", "", "/index.html", "Idc.controller.Main", null, Arrays.asList("IDC"), Arrays.asList("/stylesheets/etc/all.css"));
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/js/idc", new BundleResolver(bundleContext), idc);
//        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.energyict.mdc.issue.datacollection.extjs/src/main/web/js/idc", new FileResolver(), idc);
        registration = bundleContext.registerService(HttpResource.class, resource, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }
}