package com.energyict.protocolimplv2.common;

import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of a DeviceMessageSupport.
 * We can use this while developing a Protocol, but it should
 * not be used or adjusted for 'real' protocols, make your own
 * implementation if you need it!
 *
 * Copyrights EnergyICT
 * Date: 10/07/13
 * Time: 9:07
 */
public class TempDeviceMessageSupport implements DeviceMessageSupport {

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return this.getCollectedDataFactory().createEmptyCollectedMessageList();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.getCollectedDataFactory().createEmptyCollectedMessageList();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

}
