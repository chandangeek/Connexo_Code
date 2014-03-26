package com.energyict.comserver.core.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Arrays;

/**
 * Copyrights EnergyICT
 * Date: 28/10/13
 * Time: 10:18
 */
public class Activator implements BundleActivator {

    private volatile ServiceRegistration<HttpResource> registration;

    public void start(BundleContext bundleContext) throws Exception {
        String alias = "/mdc";
//        List<Script> scripts = new ArrayList<>();
//        scripts.add(new Script("jquery","/bla/bla/bla"));
//        DefaultStartPage mdc = new DefaultStartPage("Mdc", "", "/index.html", "Mdc.controller.Main",scripts);
        DefaultStartPage mdc = new DefaultStartPage("Mdc", "", "/index.html", "Mdc.controller.Main",null,Arrays.asList("MDC"));
        HttpResource resource = new HttpResource(alias, "/js/mdc" , new BundleResolver(bundleContext), mdc);
//        HttpResource resource = new HttpResource(alias, "C:/jupiterrepo/comserver-all/com.energyict.comserver.comserver.core.extjs/src/main/web/js/mdc" , new FileResolver(), mdc);
        registration = bundleContext.registerService(HttpResource.class, resource , null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

}
