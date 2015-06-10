package com.elster.jupiter.appserver.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.FileResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.appserver.extjs", service = {TranslationKeyProvider.class},
        property = {"name=" + AppServerUIInstaller.COMPONENT_NAME + "-UI"}, immediate = true)
public class AppServerUIInstaller implements TranslationKeyProvider {

    public static final String HTTP_RESOURCE_ALIAS = "/apr";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/appserver";
    private static final Logger LOGGER = Logger.getLogger(AppServerUIInstaller.class.getName());
    public static final String COMPONENT_NAME = "APR";
    private volatile ServiceRegistration<HttpResource> registration;

    public AppServerUIInstaller() {
    }

    @Activate
    public void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
        // EXAMPLE: Below is how to enable local development mode.
//        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.export.extjs/src/main/web/js/export", new FileResolver());
//        HttpResource resource =  new HttpResource(HTTP_RESOURCE_ALIAS, "D:/Work/Jupiter/Sources/copl/com.elster.jupiter.appserver.extjs/src/main/web/js/appserver", new FileResolver());
        registration = context.registerService(HttpResource.class, resource, null);
    }

    @Deactivate
    public void deactivate() {
        registration.unregister();
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        try {
            return SimpleTranslationKey.loadFromInputStream(this.getClass().getClassLoader().getResourceAsStream("i18n.properties"));
        } catch (IOException e) {
            LOGGER.severe("Failed to load translations for the '" + COMPONENT_NAME + "' component bundle.");
        }
        return null;
    }

}