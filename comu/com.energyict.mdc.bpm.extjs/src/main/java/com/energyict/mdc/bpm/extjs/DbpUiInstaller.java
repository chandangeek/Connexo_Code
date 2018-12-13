/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.extjs;

import com.elster.jupiter.http.whiteboard.BundleResolver;
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

@Component(name = "com.energyict.mdc.bpm.extjs", service = {TranslationKeyProvider.class},
        property = {"name=" + DbpUiInstaller.COMPONENT_NAME + "-UI"}, immediate = true)
public class DbpUiInstaller implements TranslationKeyProvider {

    public static final String COMPONENT_NAME = "DBP";
    public static final String HTTP_RESOURCE_ALIAS = "/dbp";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/dbp";
    private static final Logger LOGGER = Logger.getLogger(DbpUiInstaller.class.getName());
    private volatile ServiceRegistration<HttpResource> registration;

    public DbpUiInstaller() {
    }

    @Activate
    public void activate(BundleContext context) {
             HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
//         EXAMPLE: Below is how to enable local development mode.
//       HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "C:\\Work\\Sources\\comu\\com.energyict.mdc.bpm.extjs\\src\\main\\web\\js\\dbp", new FileResolver());
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
