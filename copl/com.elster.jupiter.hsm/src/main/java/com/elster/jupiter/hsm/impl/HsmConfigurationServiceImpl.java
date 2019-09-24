/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;


import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.loader.HsmResourceLoader;
import com.elster.jupiter.hsm.impl.loader.HsmResourceLoaderFactory;
import com.elster.jupiter.hsm.impl.resources.HsmReloadableConfigResource;
import com.elster.jupiter.hsm.impl.resources.HsmReloadableJssConfigResource;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.HsmNotConfiguredException;

import com.atos.worldline.jss.configuration.RawConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.hsm.impl.HsmConfigurationServiceImpl", service = {HsmConfigurationService.class}, immediate = true, property = "name=" + "HsmConfigurationServiceImpl")
public class HsmConfigurationServiceImpl implements HsmConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(HsmConfigurationServiceImpl.class);


    private static final String HSM_CONFIGURATION = "com.elster.jupiter.hsm.config";
    private static final String HSM_CONFIGURATION_RELOAD = "com.elster.jupiter.hsm.config.reload";
    private final long DEFAULT_RELOAD_TIME = 30 * 1000L;

    private HsmConfiguration hsmConfiguration;
    private RawConfiguration rawConfiguration;
    private HsmConfigurationObserver hsmConfigurationObserver;

    private String bundleConfigFile;

    @Activate
    public void activate(BundleContext context) {
        bundleConfigFile = context.getProperty(HSM_CONFIGURATION);
        Boolean automaticConfigReload = getAutomaticConfig(context);
        // this is ok while this is a service and therefore a singleton
        hsmConfigurationObserver = new HsmConfigurationObserver(this, bundleConfigFile, automaticConfigReload, DEFAULT_RELOAD_TIME);
        new Thread(hsmConfigurationObserver, "HSM-reloader").start();
    }

    @Deactivate
    public void deactivate() {
        hsmConfigurationObserver.stop();
    }

    @Override
    public HsmConfiguration getHsmConfiguration() throws HsmNotConfiguredException {
        if (hsmConfiguration == null) {
            throw new HsmNotConfiguredException();
        }
        return hsmConfiguration;
    }


    @Override
    public Collection<String> getLabels() throws HsmNotConfiguredException {
        if (rawConfiguration == null) {
            throw new HsmNotConfiguredException();
        }
        return rawConfiguration.getRawLabels().stream().map(s -> s.name()).collect(Collectors.toList());
    }

    @Override
    public void reload() {
        try {
            HsmResourceLoader<HsmConfiguration> hsmConfigLoader = HsmResourceLoaderFactory.getInstance(new HsmReloadableConfigResource(new File(bundleConfigFile)));
            HsmResourceLoader<RawConfiguration> jssLoader = HsmResourceLoaderFactory.getInstance(new HsmReloadableJssConfigResource(new File(hsmConfigLoader.load().getJssInitFile())));
            this.hsmConfiguration = hsmConfigLoader.load();
            this.rawConfiguration = jssLoader.load();
        } catch (HsmBaseException e) {
            logger.error("Unable to configure/load JSS", e);
        }
    }

    private Boolean getAutomaticConfig(BundleContext context) {
        String property = context.getProperty(HSM_CONFIGURATION_RELOAD);
        if (property != null) {
            return Boolean.valueOf(property.trim());
        }
        return false;
    }

}
