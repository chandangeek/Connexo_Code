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
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/system";

    public static final String APP_KEY = "SYS";
    public static final String APP_NAME = "Connexo System Admin";
    public static final String APP_ICON = "connexo";

    private volatile ServiceRegistration<App> registration;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APP_NAME));
        App app = new App(APP_KEY, APP_NAME, APP_ICON, HTTP_RESOURCE_ALIAS, resource);

        registration = context.registerService(App.class, app, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

}
