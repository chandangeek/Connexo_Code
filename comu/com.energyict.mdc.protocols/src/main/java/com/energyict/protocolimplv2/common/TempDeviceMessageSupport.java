package com.energyict.protocolimplv2.common;

import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.elster.jupiter.properties.PropertySpec;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.noneOf(DeviceMessageId.class);
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
