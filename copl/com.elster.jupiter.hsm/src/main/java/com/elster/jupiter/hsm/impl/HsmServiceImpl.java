/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmService;

import com.atos.worldline.jss.internal.spring.JssEmbeddedRuntimeConfig;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;


@Component(name = "com.elster.jupiter.hsm", service = {HsmService.class}, immediate = true, property = "name=" + HsmService.COMPONENTNAME)
@SuppressWarnings("unused")
public class HsmServiceImpl implements HsmService {

    @Activate
    public void activate(BundleContext context) {
    }


}
