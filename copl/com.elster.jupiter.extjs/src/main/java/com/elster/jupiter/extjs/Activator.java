package com.elster.jupiter.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.Script;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activator implements BundleActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/ext";

    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        List<Script> scripts = new ArrayList<>();
        Map<String, String> dependencies = new HashMap<>();

        dependencies.put("Skyline", "/packages/uni-theme-skyline/src");

        DefaultStartPage startPage = new DefaultStartPage(
                "Ext",
                "",
                "",
                "",
                scripts,
                null,
                null,
                dependencies
        );

        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/js/ext", new BundleResolver(bundleContext), startPage);
        registration = bundleContext.registerService(HttpResource.class, resource, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

}