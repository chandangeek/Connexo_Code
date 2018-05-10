/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;
import com.atos.worldline.jss.internal.spring.JssEmbeddedRuntimeConfig;
import org.osgi.service.component.annotations.Component;

import java.io.File;

@Component(name = "com.elster.jupiter.hsm.console", service = {HsmConfigurationService.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=initJss", "osgi.command.function=encrypt"}, immediate = true)
public class HsmConfigurationService {


    private boolean initialized = false;

    public void initJss(String file) {
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

    /**
     * This method is needed while spring components of JSS requires resolving of some property files.
     * All this together with OSGi behavior described at  https://stackoverflow.com/questions/2198928/better-handling-of-thread-context-classloader-in-osgi
     * requires setting of context class loader. This seems to fix init issues for spring context but not necessary fixing later problems that can be induced by same root cause (wrong class loader in context)
     */
    private void setClassLoader() {
        ClassLoader classLoader = JssEmbeddedRuntimeConfig.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
    }


    public boolean isInit() {
        return initialized;
    }
}
