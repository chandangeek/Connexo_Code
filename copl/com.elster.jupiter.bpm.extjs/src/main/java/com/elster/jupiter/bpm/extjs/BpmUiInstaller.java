package com.elster.jupiter.bpm.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.bpm.extjs", service = {TranslationKeyProvider.class},
        property = {"name=" + BpmUiInstaller.COMPONENT_NAME + "-UI"}, immediate = true)
public class BpmUiInstaller implements TranslationKeyProvider {

    public static final String APP_KEY = "BPM";
    public static final String COMPONENT_NAME = "BPM";
    public static final String HTTP_RESOURCE_ALIAS = "/bpm";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/bpm";

    private static final Logger LOGGER = Logger.getLogger(BpmUiInstaller.class.getName());
    private volatile ServiceRegistration<HttpResource> registration;
    private volatile License license;

    public BpmUiInstaller() {
    }


    @Activate
    public void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
        // EXAMPLE: Below is how to enable local development mode.
//      HttpResource resource =  new HttpResource(HTTP_RESOURCE_ALIAS, "/home/lvz/Documents/Workspace/Jupiter/com.elster.jupiter.bpm.extjs/src/main/web/js/bpm", new FileResolver());
        registration = context.registerService(HttpResource.class, resource, null);
    }

    @Deactivate
    public void deactivate() {
        registration.unregister();
    }

    @Reference(target="(com.elster.jupiter.license.application.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
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