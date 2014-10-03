package com.elster.jupiter.system.app;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpActivator;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator extends HttpActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/systemadmin";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/system";

    public static final String APP_NAME = "Connexo System Admin";
    public static final String APP_ICON = "connexo";

    private volatile ServiceRegistration<App> registration;

    public Activator() {
        super(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME);
        // EXAMPLE: Below is how to enable development mode.
//        super(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.system.app/src/main/web/js/system", true);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, createResolver(context), new DefaultStartPage(APP_NAME));
        App app = new App(APP_NAME, APP_ICON, HTTP_RESOURCE_ALIAS, resource);

        registration = context.registerService(App.class, app, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

}
