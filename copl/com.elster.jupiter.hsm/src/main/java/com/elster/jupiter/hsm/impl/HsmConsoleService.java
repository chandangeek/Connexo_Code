/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;
import com.atos.worldline.jss.configuration.RawConfigurationConverter;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.io.InputStream;

@Component(name = "com.elster.jupiter.hsm.console", service = {HsmConsoleService.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=initHsm"}, immediate = true)

public class HsmConsoleService {


    public void initHsm(String file) {
        try {
            //InputStream is = this.getClass().getClassLoader().getResourceAsStream("hsm-runtime-configuration.json");
            File f = new File(file);
            RawConfiguration cfg = new HsmConfigLoader().load(f);
            JSSRuntimeControl.initialize();
            JSSRuntimeControl.newConfiguration(cfg) ;
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println("JSS initialized");
    }


}
