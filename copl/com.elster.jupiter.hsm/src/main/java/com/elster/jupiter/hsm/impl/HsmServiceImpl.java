/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmService;

import com.atos.worldline.jss.internal.spring.JssEmbeddedRuntimeConfig;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;


@Component(name = "com.elster.jupiter.hsm", service = {HsmService.class}, immediate = true, property = "name=" + HsmService.COMPONENTNAME)
@SuppressWarnings("unused")
public class HsmServiceImpl implements HsmService {

    @Activate
    public void activate(BundleContext context) {
        // This is not due to OSGi class loader context issue, yet it seems that this class loader is not persisted later.
        // Check https://stackoverflow.com/questions/2198928/better-handling-of-thread-context-classloader-in-osgi
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = JssEmbeddedRuntimeConfig.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);

    }
}
