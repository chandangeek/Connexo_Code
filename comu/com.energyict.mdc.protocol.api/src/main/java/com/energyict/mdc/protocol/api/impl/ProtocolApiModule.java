package com.energyict.mdc.protocol.api.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageSpecificationServiceImpl;

import com.elster.jupiter.nls.NlsService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-16 (09:39)
 */
public class ProtocolApiModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(PropertySpecService.class);
        requireBinding(NlsService.class);

        bind(DeviceMessageSpecificationService.class).to(DeviceMessageSpecificationServiceImpl.class).in(Scopes.SINGLETON);
    }

}