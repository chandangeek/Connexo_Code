package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.loader.HsmResourceLoader;
import com.elster.jupiter.hsm.impl.loader.HsmResourceLoaderFactory;
import com.elster.jupiter.hsm.impl.resources.HsmReloadableConfigResource;
import com.elster.jupiter.hsm.impl.resources.HsmReloadableJssConfigResource;
import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.configuration.RawConfiguration;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class HsmConfigurationObserver implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HsmConfigurationObserver.class);
    private static final String HSM_CONFIGURATION = "com.elster.jupiter.hsm.config";
    private static final long SLEEP = 30 * 1000L;

    private final HsmConfigurationService hsmConfigurationService;
    private final BundleContext bundleContext;

    private boolean stop = false;

    public HsmConfigurationObserver(HsmConfigurationServiceImpl hsmConfigurationServices, BundleContext bundleContext) {
        this.hsmConfigurationService = hsmConfigurationServices;
        this.bundleContext = bundleContext;
    }

    public void run() {
        // this config is not refreshable and therefore do not do anything if not configured
        String bundleConfigFile = bundleContext.getProperty(HSM_CONFIGURATION);
        if (!Objects.nonNull(bundleConfigFile)) {
            return;
        }

        while (!stop) {
            configure(bundleConfigFile);
            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                logger.error("Exception caught:", e);
                e.printStackTrace();
            }
        }
    }

    private void configure(String bundleConfigFile) {
        try {
            HsmResourceLoader<HsmConfiguration> hsmConfigLoader = HsmResourceLoaderFactory.getInstance(new HsmReloadableConfigResource(new File(bundleConfigFile)));
            HsmResourceLoader<RawConfiguration> jssLoader = HsmResourceLoaderFactory.getInstance(new HsmReloadableJssConfigResource(new File(hsmConfigLoader.load().getJssInitFile())));
            hsmConfigurationService.update(hsmConfigLoader.load(), jssLoader.load());
        } catch (HsmBaseException e) {
            logger.error("Unable to configure/load JSS", e);
        }
    }

    public void stop() {
        this.stop = true;
    }

}
