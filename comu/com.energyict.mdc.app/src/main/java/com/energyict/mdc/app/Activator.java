package com.energyict.mdc.app;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpActivator;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator extends HttpActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/multisense";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/mdc";

    public static final String APP_NAME = "Connexo Multi Sense";
    public static final String APP_ICON = "connexo";

    private volatile ServiceRegistration<App> registration;

    public Activator() {
        super(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME);
        // EXAMPLE: Below is how to enable development mode.
//        super(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.energyict.mdc.app/src/main/web/js/mdc", true);
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
