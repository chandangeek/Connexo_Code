package com.elster.jupiter.hsm.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class HsmConfigurationObserver implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HsmConfigurationObserver.class);

    private final HsmConfigurationService hsmConfigurationService;
    private final String bundleConfigFile;
    private final Boolean automaticConfigReload;
    private final Long reloadTime;

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

        do {
            try {
                configure();
                Thread.sleep(reloadTime);
            } catch (Exception e) {
                logger.error("Exception caught:", e);
                e.printStackTrace();
            }
        } while (!stopRequest && automaticConfigReload);
    }

    private void configure() {
        hsmConfigurationService.reload();
    }

    public void stop() {
        this.stopRequest = true;
    }

}
