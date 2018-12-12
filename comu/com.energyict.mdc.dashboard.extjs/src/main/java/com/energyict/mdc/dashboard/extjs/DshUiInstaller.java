/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.extjs;

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

@Component(name = "com.energyict.mdc.dashboard.extjs", service = TranslationKeyProvider.class,
        property = "name=" + DshUiInstaller.COMPONENT_NAME + "-UI", immediate = true)
public class DshUiInstaller implements TranslationKeyProvider {

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DSH";
    public static final String HTTP_RESOURCE_ALIAS = "/dsh";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/dsh";
    private static final Logger LOGGER = Logger.getLogger(DshUiInstaller.class.getName());
    private volatile ServiceRegistration<HttpResource> registration;
    private volatile License license;

    public DshUiInstaller() {
    }

    @Activate
    public void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
        // EXAMPLE: Below is how to enable local development mode.
//      HttpResource resource =  new HttpResource(HTTP_RESOURCE_ALIAS, "C:/jupiterrepo/comserver-all/com.energyict.mdc.dashboard.extjs/src/main/web/js/dsh", new FileResolver());
        registration = context.registerService(HttpResource.class, resource, null);
    }

    @Deactivate
    public void deactivate() {
        registration.unregister();
    }

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
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