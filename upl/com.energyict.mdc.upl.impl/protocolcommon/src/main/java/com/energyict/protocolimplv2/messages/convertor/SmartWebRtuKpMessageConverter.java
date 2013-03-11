package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.comserver.adapters.common.LegacyMessageConverter;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SmartWebRtuKpMessageConverter implements LegacyMessageConverter {

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.<DeviceMessageSpec>asList(
                ContactorDeviceMessage.CONTACTOR_OPEN,
                ContactorDeviceMessage.CONTACTOR_CLOSE);
    }

    @Override
    public CollectedMessage executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CollectedData updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
