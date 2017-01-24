package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

/**
 * @author sva
 * @since 4/07/13 - 10:00
 */
public class CTRDeviceProtocolMessageAcknowledgement extends DeviceProtocolMessageAcknowledgement {

    public CTRDeviceProtocolMessageAcknowledgement(MessageIdentifier messageIdentifier) {
        super(messageIdentifier);
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CTRUpdateDeviceMessage(this, getComTaskExecution(), serviceProvider);
    }

}