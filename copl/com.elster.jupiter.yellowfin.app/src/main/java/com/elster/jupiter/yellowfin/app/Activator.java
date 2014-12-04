package com.elster.jupiter.yellowfin.app;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.license.License;
import org.osgi.framework.*;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


public class Activator implements BundleActivator {

    public static final String APP_KEY = "YFN";
    public static final String APP_NAME = "Reports";
    public static final String APP_ICON = "connexo";

    public static final String HTTP_RESOURCE_ALIAS = "/reports";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/yellowfin";

    private static final String YELLOWFIN_URL = "com.elster.jupiter.yellowfin.url";
    private static final String DEFAULT_YELLOWFIN_URL = "http://localhost:8081";

    private ServiceTracker<License, License> serviceTracker;
    private volatile ServiceRegistration<App> registration;
    private volatile License license;

    private BundleContext context;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        serviceTracker = new ServiceTracker<License, License>(context, License.class.getName(), new LicenseTracker());
        serviceTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        serviceTracker.close();
        serviceTracker = null;
    }

    private class LicenseTracker implements ServiceTrackerCustomizer<License, License> {

        @Override
        public License addingService(ServiceReference<License> reference) {
            if(reference.getProperty("com.elster.jupiter.license.application.key").toString().equals(APP_KEY)){
                String url = context.getProperty(YELLOWFIN_URL);
                if (url == null || !url.startsWith("http://")) {
                    url = DEFAULT_YELLOWFIN_URL;
                }

                if(context != null){
                    HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APP_NAME));
                    App app = new App(APP_KEY, APP_NAME, APP_ICON, HTTP_RESOURCE_ALIAS, resource, url);

                    registration = context.registerService(App.class, app, null);
                }
            }

            return context.getService(reference);
        }

        @Override
        public void modifiedService(ServiceReference<License> reference, License service) {
        }

        @Override
        public void removedService(ServiceReference<License> reference, License service) {
            if(reference.getProperty("com.elster.jupiter.license.application.key").toString().equals(APP_KEY)){
                registration.unregister();
            }
        }
    }
}
