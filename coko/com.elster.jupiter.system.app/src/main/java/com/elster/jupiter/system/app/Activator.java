package com.elster.jupiter.system.app;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Activator implements BundleActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/systemadmin";

    private volatile ServiceRegistration<App> registration;

    public void start(BundleContext bundleContext) throws Exception {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/js/system", new BundleResolver(bundleContext), new DefaultStartPage("System Admin App"));
//        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.system.app/src/main/web/js/system", new FileResolver(), new DefaultStartPage("System Admin App"));
        App systemAdminApp = new App("Connexo System Admin", "connexo", HTTP_RESOURCE_ALIAS, resource);
        Class<App> appClass = App.class;
        registration = bundleContext.registerService(appClass, systemAdminApp, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

}
