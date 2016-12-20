package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class LoadProfileConfigRequest extends AbstractConfigMessage {

    public LoadProfileConfigRequest(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        int enable = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.ENABLE_DISABLE).getValue());
        int intervalInMinutes = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.CONFIG_LOAD_PROFILE_INTERVAL).getValue());
        int maxNumberOfRecords = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.MAX_NUMBER_RECORDS).getValue());
        if (enable != 0 && enable != 1) {
            failMessage("Load profile configuration message failed, invalid arguments");
            return;
        }
        if (intervalInMinutes != 1 && intervalInMinutes != 2 && intervalInMinutes != 3 && intervalInMinutes != 5 && intervalInMinutes != 6 && intervalInMinutes != 0x0A && intervalInMinutes != 0x0C && intervalInMinutes != 0x0F && intervalInMinutes != 0x14 && intervalInMinutes != 0x1E && intervalInMinutes != 0x3C && intervalInMinutes != 0x78 && intervalInMinutes != 0xF0) {
            failMessage("Load profile configuration message failed, invalid arguments");
            return;
        }
        trackingId = getAce4000().getObjectFactory().sendLoadProfileConfiguration(enable, intervalInMinutes, maxNumberOfRecords);
    }
}