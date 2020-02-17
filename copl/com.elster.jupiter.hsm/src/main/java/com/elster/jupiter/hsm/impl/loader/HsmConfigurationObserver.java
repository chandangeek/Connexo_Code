package com.elster.jupiter.hsm.impl.loader;

import com.elster.jupiter.hsm.impl.HsmConfigurationService;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.resources.HsmReloadableConfigResource;
import com.elster.jupiter.hsm.impl.resources.HsmReloadableJssConfigResource;
import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.configuration.RawConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class HsmConfigurationObserver implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HsmConfigurationObserver.class);

    private final HsmConfigurationService hsmConfigurationService;
    private final String bundleConfigFile;
    private final Boolean automaticConfigReload;
    private final Long reloadTime;

    private boolean observerRunning = false;
    private boolean stopRequest = false;

    public HsmConfigurationObserver(HsmConfigurationService hsmConfigurationService, String bundleConfigFile, Boolean automaticConfigReload, Long reloadTime) {
        this.hsmConfigurationService = hsmConfigurationService;
        this.bundleConfigFile = bundleConfigFile;
        this.automaticConfigReload = automaticConfigReload;
        this.reloadTime = reloadTime;
    }

    public void run() {
        // this config is not refreshable and therefore do not do anything if not configured
        if (StringUtils.isEmpty(bundleConfigFile)) {
            return;
        }
        observerRunning = true;
        do {
            try {
                configure(bundleConfigFile);
                Thread.sleep(reloadTime);
            } catch (InterruptedException e) {
                logger.error("Exception caught:", e);
            }
        } while (!stopRequest && automaticConfigReload);
        observerRunning = false;
    }

    private void configure(String bundleConfigFile) {
        try {
            HsmResourceLoader<HsmConfiguration> hsmConfigLoader = HsmResourceLoaderFactory.getInstance(HsmReloadableConfigResource.getInstance(new File(bundleConfigFile)));
            HsmResourceLoader<RawConfiguration> jssLoader = HsmResourceLoaderFactory.getInstance(HsmReloadableJssConfigResource.getInstance(new File(hsmConfigLoader.load().getJssInitFile())));
            hsmConfigurationService.set(hsmConfigLoader.load(), jssLoader.load());
        } catch (HsmBaseException e) {
            logger.error("Unable to configure/load JSS", e);
        }
    }

    public void stop() throws HsmBaseException {
        if (!observerRunning) {
            return;
        }
        this.stopRequest = true;
        HsmResourceLoader<HsmConfiguration> hsmConfigLoader = HsmResourceLoaderFactory.getInstance(HsmReloadableConfigResource.getInstance(new File(bundleConfigFile)));
        HsmReloadableJssConfigResource.getInstance(new File(hsmConfigLoader.load().getJssInitFile())).close();
    }

}
