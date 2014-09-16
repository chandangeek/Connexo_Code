package com.elster.jupiter.issue.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.FileResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.Script;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activator implements BundleActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/isu";

    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        List<Script> scripts = new ArrayList<>();

        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("Ext.ux.Rixo", "/resources/js/Ext/ux/Rixo");

        DefaultStartPage issueStartPage = new DefaultStartPage(
                "Isu",
                "",
                "/index.html",
                "Isu.controller.Main",
                scripts,
                Arrays.asList("ISU"),
                Arrays.asList("/resources/css/isu.css"),
                dependencies
        );

        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/js/issue", new BundleResolver(bundleContext), issueStartPage);
//        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.issue.extjs/src/main/web/js/issue", new FileResolver(), issueStartPage);
        registration = bundleContext.registerService(HttpResource.class, resource, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

}
