/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;
import org.osgi.service.component.annotations.Component;

import java.io.File;

@Component(name = "com.elster.jupiter.hsm.console", service = {HsmConsoleService.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=initHsm"}, immediate = true)

public class HsmConsoleService {


    public void initHsm(String file) {
        try {
            File f = new File(file);
            RawConfiguration cfg = new HsmConfigLoader().load(f);
            JSSRuntimeControl.initialize();
            JSSRuntimeControl.newConfiguration(cfg) ;
        } catch (Throwable e){
            System.out.println(e);
            throw(e);
        }
        System.out.println("JSS initialized");
    }


}
