package com.energyict.protocolimplv2.dlms.common.writers;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.properties.PropertySpec;

public interface Message {

    CollectedMessage execute(OfflineDeviceMessage message);

    /**
     * This method likely should not exist but it is present in upper model therefore needed. However in the spirit of fail fast we should throw an exception if
     * for some strange reason this method is being called.
     * @return nothing
     * @throws ProtocolException always
     */
    default CollectedMessage update(OfflineDeviceMessage message) throws ProtocolException {
        throw new ProtocolException("Update command not implemented");
    }

    DeviceMessageSpec asMessageSpec();

    default String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }
}
