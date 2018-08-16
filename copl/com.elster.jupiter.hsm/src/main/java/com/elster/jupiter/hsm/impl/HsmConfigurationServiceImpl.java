/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;


import com.elster.jupiter.hsm.impl.resources.HsmConfigRefreshableResourceBuilder;
import com.elster.jupiter.hsm.impl.config.HsmJssConfigLoader;
import com.elster.jupiter.hsm.impl.context.HsmClassLoaderHelper;
import com.elster.jupiter.hsm.impl.resources.HsmResourceReloader;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;

import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.util.Objects;

@Component(name = "com.elster.jupiter.hsm.impl.HsmConfigurationServiceImpl", service = {HsmConfigurationService.class}, immediate = true, property = "name=" + HsmConfigurationServiceImpl.COMPONENTNAME)
public class HsmConfigurationServiceImpl implements HsmConfigurationService {

    static final String COMPONENTNAME = "HsmConfigurationServiceImpl";
    private boolean initialized = false;
    private static final String HSM_CONFIGURATION = "com.elster.jupiter.hsm.config";

    private HsmResourceReloader<HsmConfiguration> hsmConfigurationLoader;

    public void init(String file) {
        try {
            File f = new File(file);
            RawConfiguration cfg = new HsmJssConfigLoader().load(f);
            JSSRuntimeControl.initialize();
            JSSRuntimeControl.newConfiguration(cfg);
            this.initialized = true;
        } catch (Throwable e) {
            System.out.println(e);
            throw (e);
        }
        System.out.println("JSS initialized");
    }

    private void waitInit() {
        System.out.println("Waiting for HSM initialization, please be patient...");
        if (!JSSRuntimeControl.waitSecondsForAvailableHsms(10)) {
            throw new RuntimeException("No HSM available! Initialization failed");
        }
    }

    public void checkInit() {
        if (!initialized) {
            throw new RuntimeException("JSS not initialized!");
        }
    }

    @Activate
    public void activate(BundleContext context) {
        HsmClassLoaderHelper.setClassLoader();

        String configFile = context.getProperty(HSM_CONFIGURATION);
        if (Objects.nonNull(configFile)) {
            try {
                this.hsmConfigurationLoader = new HsmResourceReloader<>(new HsmConfigRefreshableResourceBuilder(new File(configFile)));
                init(hsmConfigurationLoader.load().getJssInitFile());
            } catch (HsmBaseException e) {
                // Doing nothing while other bundles might fail because this one was not properly initialized.
                // As an example: HSM is down but we want other bundles to work even is HSM is not available for the time being, yet we need to activate it later by hand when HSM is back (like using gogo)
                //throw new RuntimeException(e);
            }
        }
    }

    @Override
    public HsmConfiguration getHsmConfiguration() throws HsmBaseException {
        return hsmConfigurationLoader.load();
    }
}
