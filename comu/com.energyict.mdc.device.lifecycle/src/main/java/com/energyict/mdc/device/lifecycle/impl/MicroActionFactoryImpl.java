package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.config.MicroAction;

import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link ServerMicroActionFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (17:28)
 */
@Component(name = "com.energyict.device.lifecycle.micro.action.factory", service = ServerMicroActionFactory.class)
@SuppressWarnings("unused")
public class MicroActionFactoryImpl implements ServerMicroActionFactory {

    @Override
    public ServerMicroAction from(MicroAction check) {
        return null;
    }

}