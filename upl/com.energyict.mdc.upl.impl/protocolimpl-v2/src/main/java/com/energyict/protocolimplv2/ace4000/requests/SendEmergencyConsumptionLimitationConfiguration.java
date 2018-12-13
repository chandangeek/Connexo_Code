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
public class SendEmergencyConsumptionLimitationConfiguration extends AbstractConfigMessage {

    public SendEmergencyConsumptionLimitationConfiguration(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        int duration = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.DURATION_MINUTES).getValue());
        int threshold = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.TRESHOLD_VALUE).getValue());
        int unit = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.TRESHOLD_UNIT).getValue());
        int overrideRate = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.OVERRIDE_RATE).getValue());
        String failMsg = "Emergency consumption limitation configuration message failed, invalid arguments";
        if (duration > 0xFFFF || duration < 0) {
            failMessage(failMsg);
            return;
        }
        if (threshold < 0) {
            failMessage(failMsg);
            return;
        }
        if (unit != 0 && unit != 1) {
            failMessage(failMsg);
            return;
        }
        if (overrideRate < 0 || overrideRate > 4) {
            failMessage(failMsg);
            return;
        }
        trackingId = getAce4000().getObjectFactory().sendEmergencyConsumptionLimitationConfigurationRequest(duration, threshold, unit, overrideRate);
    }
}