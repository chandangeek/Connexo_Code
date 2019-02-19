/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.hsm.impl;


import com.atos.worldline.jss.configuration.RawLabel;
import com.elster.jupiter.hsm.impl.resources.HsmConfigRefreshableResourceBuilder;
import com.elster.jupiter.hsm.impl.config.HsmJssConfigLoader;
import com.elster.jupiter.hsm.impl.context.HsmClassLoaderHelper;
import com.elster.jupiter.hsm.impl.resources.HsmResourceReloader;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.hsm.impl.HsmConfigurationServiceImpl",
        service = {HsmConfigurationService.class},
        immediate = true, property = "name=" + HsmConfigurationServiceImpl.COMPONENTNAME)
public class HsmConfigurationServiceImpl implements HsmConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(HsmConfigurationServiceImpl.class);

    static final String COMPONENTNAME = "HsmConfigurationServiceImpl";
    private boolean initialized = false;
    private static final String HSM_CONFIGURATION = "com.elster.jupiter.hsm.config";

    private HsmResourceReloader<HsmConfiguration> hsmConfigurationLoader;
    private RawConfiguration rawConfiguration;

    public void init(String file) {
        try {
            File f = new File(file);
            rawConfiguration = new HsmJssConfigLoader().load(f);
            JSSRuntimeControl.initialize();
            JSSRuntimeControl.newConfiguration(rawConfiguration);
            this.initialized = true;
        } catch (Throwable e) {
            LOG.error("Unable to initialize JSS", e);
            throw (e);
        }
    }

    private void waitInit() {
        LOG.debug("Waiting for HSM initialization, please be patient...");
        if (!JSSRuntimeControl.waitSecondsForAvailableHsms(10)) {
            throw new RuntimeException("No HSM available! Initialization failed");
        }
    }

    public void checkInit() throws HsmBaseException {
        if (!initialized) {
            throw new HsmBaseException("JSS not initialized!");
        }
    }

    @Activate
    public void activate(BundleContext context) {
        String configFile = context.getProperty(HSM_CONFIGURATION);
        if (Objects.nonNull(configFile)) {
            HsmClassLoaderHelper.setClassLoader();
            configureLogger();
            try {
                this.hsmConfigurationLoader = new HsmResourceReloader<>(new HsmConfigRefreshableResourceBuilder(new File(configFile)));
                init(hsmConfigurationLoader.load().getJssInitFile());
            } catch (HsmBaseException e) {
                // Doing nothing while other bundles might fail because this one was not properly initialized.
                // As an example: HSM is down but we want other bundles to work even is HSM is not available for the time being, yet we need to activate it later by hand when HSM is back (like using gogo)
                //throw new RuntimeException(e);
                LOG.warn("Unable to activate HSM bundle", e);
            }
        }
    }

    /**
     * This will try to re-configure logger used by JSS and underlying libs
     */
    private void configureLogger() {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            String logbackFile = "e:\\Elster\\Connexo\\felix\\conf\\logback.xml";
            URL resource = Thread.currentThread().getContextClassLoader().getResource(logbackFile);
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(resource);
        } catch (Exception e) {
            String msg = "Unable to re-configure logger";
            System.out.println(msg + e.getMessage() + " stackTrace:");
            e.printStackTrace();
            LOG.warn(msg, e);
        }
    }

    @Override
    public HsmConfiguration getHsmConfiguration() throws HsmBaseException {
        checkInit();
        return hsmConfigurationLoader.load();
    }

    @Override
    public Collection<String> getLabels() throws HsmBaseException {
        checkInit();
        return rawConfiguration.getRawLabels().stream().map(RawLabel::name).collect(Collectors.toList());
    }
}