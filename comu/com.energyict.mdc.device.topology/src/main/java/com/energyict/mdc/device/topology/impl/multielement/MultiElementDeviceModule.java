package com.energyict.mdc.device.topology.impl.multielement;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.energyict.mdc.device.topology.impl.ServerTopologyService;
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
        requireBinding(ServerTopologyService.class);
        requireBinding(UpgradeService.class);
        requireBinding(NlsService.class);
        bind(MultiElementDeviceService.class).to(MultiElementDeviceServiceImpl.class).in(Scopes.SINGLETON);
    }
}

