package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.comserver.adapters.common.LegacyMessageConverter;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 12:03
 */
public class ABBA1140MessageConverter implements LegacyMessageConverter {

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.<DeviceMessageSpec>asList(DeviceActionMessage.BILLING_RESET);
    }


    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setMessagingProtocol(Messaging messaging) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
