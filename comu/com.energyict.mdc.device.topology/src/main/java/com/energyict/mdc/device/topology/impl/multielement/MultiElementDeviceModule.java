package com.energyict.mdc.device.topology.impl.multielement;

import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Copyrights EnergyICT
 * Date: 20/03/2017
 * Time: 11:12
 */
public class MultiElementDeviceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MultiElementDeviceService.class).to(MultiElementDeviceServiceImpl.class).in(Scopes.SINGLETON);
    }
}

