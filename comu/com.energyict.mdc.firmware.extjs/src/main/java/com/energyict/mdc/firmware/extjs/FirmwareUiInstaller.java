package com.energyict.mdc.firmware.extjs;

import com.elster.jupiter.http.whiteboard.*;
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

/**
 * Copyrights EnergyICT
 * Date: 3/17/15
 * Time: 4:05 PM
 */
@Component(name = "com.energyict.mdc.firmware.extjs", service = {TranslationKeyProvider.class},
        property = {"name=" + FirmwareUiInstaller.COMPONENT_NAME + "-UI"}, immediate = true)
public class FirmwareUiInstaller implements TranslationKeyProvider{

    public static final String COMPONENT_NAME = "FWC";
    public static final String HTTP_RESOURCE_ALIAS = "/fwc";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/firmware";
    private static final Logger LOGGER = Logger.getLogger(FirmwareUiInstaller.class.getName());
    private volatile ServiceRegistration<HttpResource> registration;

    public FirmwareUiInstaller() {
    }

    @Activate
    public void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
        // EXAMPLE: Below is how to enable local development mode.
//        HttpResource resource =  new HttpResource(HTTP_RESOURCE_ALIAS, "/home/govanni/codebase/work/jupiter/10.1/comu/com.energyict.mdc.firmware.extjs/src/main/web/js/firmware", new FileResolver());
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
