package com.elster.insight.metering.extjs;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.http.whiteboard.FileResolver;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;

@Component(name = "com.elster.metering.extjs", service = TranslationKeyProvider.class,
        property = "name=" + ImtUiInstaller.COMPONENT_NAME + "-UI", immediate = true)
public class ImtUiInstaller implements TranslationKeyProvider {
    public static final String APP_KEY = "IMT";
    public static final String COMPONENT_NAME = "IMT";
    public static final String HTTP_RESOURCE_ALIAS = "/imt";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/imt";
    private static final Logger LOGGER = Logger.getLogger(ImtUiInstaller.class.getName());
    private volatile ServiceRegistration<HttpResource> registration;
    private volatile License license;

    public ImtUiInstaller() {

    }

    @Activate
    public void activate(BundleContext context) {
//        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
        // EXAMPLE: Below is how to enable local development mode.
        HttpResource resource =  new HttpResource(HTTP_RESOURCE_ALIAS, "/home/kurtk/Projects/git/Insight/com.elster.insight.metering.extjs/src/main/web/js/imt", new FileResolver());
        registration = context.registerService(HttpResource.class, resource, null);
    }

    @Deactivate
    public void deactivate() {
        registration.unregister();
    }

//    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
//    public void setLicense(License license) {
//        this.license = license;
//    }

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
