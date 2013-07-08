package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.mdc.meterdata.DeviceProtocolMessageAcknowledgement;
import com.energyict.mdc.meterdata.identifiers.MessageIdentifier;

/**
 * @author sva
 * @since 4/07/13 - 10:00
 */
public class CTRDeviceProtocolMessageAcknowledgement extends DeviceProtocolMessageAcknowledgement {

    public CTRDeviceProtocolMessageAcknowledgement(MessageIdentifier messageIdentifier) {
        super(messageIdentifier);
    }

    @Override
    public DeviceCommand toDeviceCommand() {
        return new CTRUpdateDeviceMessage(this);
    }
}
