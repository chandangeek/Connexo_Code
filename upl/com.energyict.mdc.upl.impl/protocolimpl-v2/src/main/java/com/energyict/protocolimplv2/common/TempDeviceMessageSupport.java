package com.energyict.protocolimplv2.common;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.MdcManager;

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
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";
    }
}
