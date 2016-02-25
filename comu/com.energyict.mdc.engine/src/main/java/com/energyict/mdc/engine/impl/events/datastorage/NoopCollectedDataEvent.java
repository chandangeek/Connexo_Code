package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link NoopDeviceCommand}
 */
public class NoopCollectedDataEvent extends AbstractCollectedDataProcessingEventImpl{

    @SuppressWarnings("unchecked")
    public NoopCollectedDataEvent(ServiceProvider serviceProvider) {
        super(serviceProvider, null);
    }

    @Override
    public String getDescription() {
        return NoopDeviceCommand.DESCRIPTION_TITLE;
    }
}
