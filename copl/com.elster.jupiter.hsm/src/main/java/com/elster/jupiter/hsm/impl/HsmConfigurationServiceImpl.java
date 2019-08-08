/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;


import com.elster.jupiter.hsm.impl.resources.HsmRefreshableConfigResourceBuilder;
import com.elster.jupiter.hsm.impl.resources.HsmRefreshableFileResourceBuilder;
import com.elster.jupiter.hsm.impl.loader.HsmJssLoader;
import com.elster.jupiter.hsm.impl.loader.HsmResourceLoader;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.hsm.impl.HsmConfigurationServiceImpl", service = {HsmConfigurationService.class}, immediate = true, property = "name=" + HsmConfigurationServiceImpl.COMPONENTNAME)
public class HsmConfigurationServiceImpl implements HsmConfigurationService {

    private static final String HSM_CONFIGURATION = "com.elster.jupiter.hsm.config";

    private static final Logger LOG = LoggerFactory.getLogger(HsmConfigurationServiceImpl.class);

    static final String COMPONENTNAME = "HsmConfigurationServiceImpl";

    private BundleContext bundleContext;
    private HsmResourceLoader<HsmConfiguration> hsmConfigurationLoader;
    private HsmJssLoader hsmJssLoader;


    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }

    private void configure() throws HsmBaseException {
        String configFile = bundleContext.getProperty(HSM_CONFIGURATION);
        if (Objects.nonNull(configFile)) {
            try {
                this.hsmConfigurationLoader = HsmResourceLoader.getInstance(new HsmRefreshableConfigResourceBuilder(new File(configFile)));
                this.hsmJssLoader = HsmJssLoader.getInstance(new HsmRefreshableFileResourceBuilder(new File(hsmConfigurationLoader.load().getJssInitFile())));
                // following line is needed while underneath it will start stop JSS context... ugly I know but effective, when I will have time I will change this
                hsmJssLoader.load();
            } catch (HsmBaseException e) {
                // Doing nothing while other bundles might fail because this one was not properly initialized.
                // As an example: HSM is down but we want other bundles to work even is HSM is not available for the time being, yet we need to activate it later by hand when HSM is back (like using gogo)
                //throw new RuntimeException(e);
                LOG.warn("Unable to activate HSM bundle", e);
            }
        } else {
            LOG.error("HSM not activated (check config.properties file)");
            throw new HsmBaseException("HSM/JSS not initialized!");
        }
    }

    @Override
    public HsmConfiguration getHsmConfiguration() throws HsmBaseException {
        configure();
        return hsmConfigurationLoader.load();
    }

    @Override
    public Collection<String> getLabels() throws HsmBaseException {
        configure();
        return hsmJssLoader.load().getRawLabels().stream().map(s -> s.name()).collect(Collectors.toList());    }

}
