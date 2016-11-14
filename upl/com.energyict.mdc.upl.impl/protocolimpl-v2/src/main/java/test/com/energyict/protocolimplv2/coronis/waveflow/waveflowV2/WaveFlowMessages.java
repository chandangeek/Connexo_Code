package test.com.energyict.protocolimplv2.coronis.waveflow.waveflowV2;

import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdw.offline.OfflineDevice;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 11:35
 * Author: khe
 */
public class WaveFlowMessages implements DeviceMessageSupport {

    private WaveFlowV2 waveFlowV2;

    public WaveFlowMessages(WaveFlowV2 waveFlowV2) {
        this.waveFlowV2 = waveFlowV2;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return "";
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }
}