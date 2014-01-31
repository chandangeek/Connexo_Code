package com.energyict.comserver.core.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.FileResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Copyrights EnergyICT
 * Date: 28/10/13
 * Time: 10:18
 */
public class Activator implements BundleActivator {

    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        String alias = "/mdc";
//        HttpResource resource = new HttpResource(alias, "/js/mdc" , new BundleResolver(bundleContext), new DefaultStartPage("Meter data collection"));
//        Comment above and uncomment next line for file based javascript serving, changing second argument as appropriate
        HttpResource resource = new HttpResource(alias, "/home/bvn/codebase/jupiter/mdc/com.energyict.comserver.comserver.core.extjs/src/main/web/js/mdc" , new FileResolver());
        registration = bundleContext.registerService(HttpResource.class, resource , null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

}
