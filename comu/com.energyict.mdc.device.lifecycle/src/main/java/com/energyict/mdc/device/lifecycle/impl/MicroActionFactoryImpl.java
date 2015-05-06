package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.ActivateConnectionTasks;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.CreateMeterActivation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.DisableCommunication;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.DisableValidation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.EnableValidation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.SetLastReading;

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
    public ServerMicroAction from(MicroAction microAction) {
        switch (microAction) {
            case SET_LAST_READING: {
                return new SetLastReading();
            }
            case ENABLE_VALIDATION: {
                return new EnableValidation();
            }
            case DISABLE_VALIDATION: {
                return new DisableValidation();
            }
            case ACTIVATE_CONNECTION_TASKS: {
                return new ActivateConnectionTasks();
            }
            case CREATE_METER_ACTIVATION: {
                return new CreateMeterActivation();
            }
            case DISABLE_COMMUNICATION: {
                return new DisableCommunication();
            }
            default: {
                throw new IllegalArgumentException("Unknown or unsupported MicroAction " + microAction.name());
            }
        }
    }

}