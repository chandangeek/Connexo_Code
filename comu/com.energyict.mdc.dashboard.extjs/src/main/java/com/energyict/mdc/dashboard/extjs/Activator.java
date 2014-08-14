package main.java.com.energyict.mdc.dashboard.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Arrays;

public class Activator {
    public static final String HTTP_RESOURCE_ALIAS = "/dashboard";
    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        DefaultStartPage dsh = new DefaultStartPage("Dsh", "", "/index.html", "Dsh.controller.Main", null, Arrays.asList("DSH"), Arrays.asList("/stylesheets/css/all.css"));
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/js/dsh", new BundleResolver(bundleContext), dsh);
        registration = bundleContext.registerService(HttpResource.class, resource, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }
}