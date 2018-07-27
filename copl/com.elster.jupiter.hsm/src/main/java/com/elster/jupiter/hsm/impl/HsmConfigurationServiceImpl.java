/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;


import com.elster.jupiter.hsm.HsmConfigurationService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.configuration.HsmConfiguration;

import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;
import com.atos.worldline.jss.internal.spring.JssEmbeddedRuntimeConfig;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Objects;

@Component(name = "com.elster.jupiter.hsm.impl.HsmConfigurationServiceImpl", service = {HsmConfigurationService.class}, immediate = true, property = "name=" + HsmConfigurationServiceImpl.COMPONENTNAME)
public class HsmConfigurationServiceImpl implements HsmConfigurationService {

    static final String COMPONENTNAME = "HsmConfigurationServiceImpl";
    private boolean initialized = false;
    private static final String HSM_CONFIGURATION = "com.elster.jupiter.hsm.config";

    private HsmConfiguration hsmConfiguration;

    public void init(String file) {
        try {
            setClassLoader();

            File f = new File(file);
            RawConfiguration cfg = new HsmConfigLoader().load(f);
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

    /**
     * This method is needed while spring components of JSS requires resolving of some property files.
     * All this together with OSGi behavior described at  https://stackoverflow.com/questions/2198928/better-handling-of-thread-context-classloader-in-osgi
     * requires setting of context class loader. This seems to fix init issues for spring context but not necessary fixing later problems that can be induced by same root cause (wrong class loader in context)
     */
    private void setClassLoader() {
        // setting up a custom classloader is needed while we have resources in parent classloader path (AppLauncher), in felix conf folder but also embedded in JSS jar file provided by ATOS.
        // besides following lines we must assure that JVM will be started having felix conf folder in class path
        ClassLoader hsmClassLoader = JssEmbeddedRuntimeConfig.class.getClassLoader();
        URLClassLoader appClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        ClassLoader loader = new URLClassLoader(appClassLoader.getURLs(), hsmClassLoader);
        Thread.currentThread().setContextClassLoader(loader);
    }


    public void checkInit() {
        if (!initialized) {
            throw new RuntimeException("JSS not initialized!");
        }
    }

    @Activate
    public void activate(BundleContext context) {
        String configFile = context.getProperty(HSM_CONFIGURATION);
        if (Objects.nonNull(configFile)) {
            try {
                this.hsmConfiguration = new HsmConfigurationPropFileImpl(configFile);
                init(hsmConfiguration.getJssInitFile());
            } catch (HsmBaseException e) {
                // Doing nothing while other bundles might fail because this one was not properly initialized.
                // As an example: HSM is down but we want other bundles to work even is HSM is not available for the time being, yet we need to activate it later by hand when HSM is back (like using gogo)
                //throw new RuntimeException(e);
            }
        }
    }

    @Override
    public HsmConfiguration getHsmConfiguration() {
        return hsmConfiguration;
    }
}
