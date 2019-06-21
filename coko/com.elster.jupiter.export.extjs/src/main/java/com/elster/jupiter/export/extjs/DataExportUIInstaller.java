/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.extjs;

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

@Component(name = "com.elster.jupiter.export.extjs", service = {TranslationKeyProvider.class},
        property = {"name=" + DataExportUIInstaller.COMPONENT_NAME + "-UI"}, immediate = true)
public class DataExportUIInstaller implements TranslationKeyProvider {

    public static final String HTTP_RESOURCE_ALIAS = "/dxp";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/export";
    private static final Logger LOGGER = Logger.getLogger(DataExportUIInstaller.class.getName());
    public static final String COMPONENT_NAME = "DES";
    private volatile ServiceRegistration<HttpResource> registration;

    public DataExportUIInstaller() {
    }

    @Activate
    public void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
        // EXAMPLE: Below is how to enable local development mode.
        //HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "C:\\git10.7\\connexo\\coko\\com.elster.jupiter.export.extjs\\src\\main\\web\\js\\export", new FileResolver());
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