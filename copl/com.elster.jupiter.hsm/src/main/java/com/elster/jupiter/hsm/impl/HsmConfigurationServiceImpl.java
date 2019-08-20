/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;


import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.configuration.RawConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Collection;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.hsm.impl.HsmConfigurationServiceImpl", service = {HsmConfigurationService.class}, immediate = true, property = "name=" + "HsmConfigurationServiceImpl")
public class HsmConfigurationServiceImpl implements HsmConfigurationService {

    private HsmConfiguration hsmConfiguration;
    private RawConfiguration rawConfiguration;
    private HsmConfigurationObserver hsmConfigurationObserver;

    @Activate
    public void activate(BundleContext context) {
        // this is ok while this is a service and therefore a singleton
        hsmConfigurationObserver = new HsmConfigurationObserver(this, context);
        new Thread(hsmConfigurationObserver, "HSM-reloader").start();
    }

    @Deactivate
    public void deactivate() {
        hsmConfigurationObserver.stop();
    }

    @Override
    public HsmConfiguration getHsmConfiguration() throws HsmBaseException {
        if (hsmConfiguration == null) {
            throw new HsmBaseException("HSM/JSS not initialized!");
        }
        return hsmConfiguration;
    }


    @Override
    public Collection<String> getLabels() throws HsmBaseException {
        if (rawConfiguration == null) {
            throw new HsmBaseException("HSM/JSS not initialized!");
        }
        return rawConfiguration.getRawLabels().stream().map(s -> s.name()).collect(Collectors.toList());
    }

    @Override
    public void update(HsmConfiguration hsmConfiguration, RawConfiguration rawConfiguration) {
        this.hsmConfiguration = hsmConfiguration;
        this.rawConfiguration = rawConfiguration;
    }

}
