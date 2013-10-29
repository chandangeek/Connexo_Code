package com.energyict.comserver.core.extjs;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Copyrights EnergyICT
 * Date: 28/10/13
 * Time: 10:18
 */
public class Activator implements BundleActivator {

//    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
//        String alias = "/cms";
//        HttpResource resource = new HttpResource(alias, "/js/cms" , new BundleResolver(bundleContext));
//        Comment above and uncomment next line for file based javascript serving, changing second argument as appropriate
//        HttpResource resource = new HttpResource(alias, "/home/lvz/Documents/Workspace/Jupiter/meteringextjs/js/mtr" , new FileResolver());
//        registration = bundleContext.registerService(HttpResource.class, resource , null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
//        registration.unregister();
    }

}
