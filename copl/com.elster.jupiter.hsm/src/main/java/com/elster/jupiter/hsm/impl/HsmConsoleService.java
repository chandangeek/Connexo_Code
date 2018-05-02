/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmModule;
import com.elster.jupiter.hsm.HsmService;
import com.elster.jupiter.hsm.OperationStatus;

import com.atos.worldline.jss.configuration.DefaultRawConfiguration;
import com.atos.worldline.jss.configuration.RawConfiguration;
import com.atos.worldline.jss.configuration.RawConfigurationConverter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

@Component(name = "com.elster.jupiter.hsm.console", service = {HsmConsoleService.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=initHsm"}, immediate = true)

public class HsmConsoleService {

    private volatile HsmService hsmService;

    public void initHsm() throws URISyntaxException {
        InputStream hsmRuntimeConfigurationFile = this.getClass().getResourceAsStream("/hsm-runtime-configuration.json");
        final RawConfiguration rawConfiguration = new RawConfigurationConverter().loadFromInputStream(hsmRuntimeConfigurationFile);
        new HsmModule().init(rawConfiguration);
        System.out.println(new OperationStatus("JSM initialized", 200, null));
    }

    @Reference
    public void setHsmService(HsmService hsmService) {
        this.hsmService = hsmService;
    }

}
