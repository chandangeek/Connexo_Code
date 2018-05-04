/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;
import com.atos.worldline.jss.configuration.RawConfigurationConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import java.io.InputStreamReader;

@Component(name = "com.elster.jupiter.hsm.console", service = {HsmConsoleService.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=initHsm"}, immediate = true)

public class HsmConsoleService {


    public void initHsm() {
        try {
            JSSRuntimeControl.initialize();
            InputStream hsmRuntimeConfigurationFile = this.getClass().getResourceAsStream("/hsm-runtime-configuration.json");
            final RawConfiguration rawConfiguration = new RawConfigurationConverter().loadFromInputStream(hsmRuntimeConfigurationFile);
            JSSRuntimeControl.newConfiguration(rawConfiguration) ;
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println("JSS initialized");
    }


}
