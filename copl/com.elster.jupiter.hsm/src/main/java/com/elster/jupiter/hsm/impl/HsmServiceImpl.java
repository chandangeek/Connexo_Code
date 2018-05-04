/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmService;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;


@Component(name = "com.elster.jupiter.hsm", service = {HsmService.class}, immediate = true, property = "name=" + HsmService.COMPONENTNAME)
@SuppressWarnings("unused")
public class HsmServiceImpl implements HsmService {

    @Activate
    public void activate(BundleContext context) {
    }
}
