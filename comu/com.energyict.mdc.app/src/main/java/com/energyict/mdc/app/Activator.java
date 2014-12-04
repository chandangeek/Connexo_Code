package com.energyict.mdc.app;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.Dictionary;
import java.util.Hashtable;

public class Activator implements BundleActivator {

    public static final String HTTP_RESOURCE_ALIAS = "/multisense";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/mdc";

    public static final String APP_KEY = "MDC";
    public static final String APP_NAME = "Connexo Multi Sense";
    public static final String APP_ICON = "connexo";

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

    //@Reference(target="(com.elster.jupiter.license.application.key=" + APP_KEY  + ")")
    //public void setLicense(License license) {
    //    this.license = license;
    //}

    private class LicenseTracker implements ServiceTrackerCustomizer<License, License> {

        @Override
        public License addingService(ServiceReference<License> reference) {
            if(reference.getProperty("com.elster.jupiter.license.application.key").toString().equals(APP_KEY)){
                HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APP_NAME));
                App app = new App(APP_KEY, APP_NAME, APP_ICON, HTTP_RESOURCE_ALIAS, resource);

                registration = context.registerService(App.class, app, null);
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
