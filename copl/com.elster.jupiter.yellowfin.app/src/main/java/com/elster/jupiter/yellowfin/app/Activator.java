package com.elster.jupiter.yellowfin.app;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Activator implements BundleActivator {

    public static final String APP_KEY = "YFN";
    public static final String APP_NAME = "Reports";
    public static final String APP_ICON = "connexo";

    public static final String HTTP_RESOURCE_ALIAS = "/reports";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/yellowfin";

    private static final String YELLOWFIN_URL = "com.elster.jupiter.yellowfin.url";
    private static final String DEFAULT_YELLOWFIN_URL = "http://localhost:8081";

    private volatile ServiceRegistration<App> registration;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {

        String url = context.getProperty(YELLOWFIN_URL);
        if (url == null || !url.startsWith("http://")) {
            url = DEFAULT_YELLOWFIN_URL;
        }

        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APP_NAME));
        App app = new App(APP_KEY, APP_NAME, APP_ICON, HTTP_RESOURCE_ALIAS, resource, url);

        registration = context.registerService(App.class, app, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

}
