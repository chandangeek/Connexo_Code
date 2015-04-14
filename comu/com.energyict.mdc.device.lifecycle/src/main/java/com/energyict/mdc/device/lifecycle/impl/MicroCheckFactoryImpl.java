package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.nls.TranslationKeyProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link ServerMicroCheckFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (17:28)
 */
@Component(name = "com.energyict.device.lifecycle.micro.check.factory", service = ServerMicroCheckFactory.class)
@SuppressWarnings("unused")
public class MicroCheckFactoryImpl implements ServerMicroCheckFactory {

    @Override
    public ServerMicroCheck from(MicroCheck check) {
        return null;
    }

}