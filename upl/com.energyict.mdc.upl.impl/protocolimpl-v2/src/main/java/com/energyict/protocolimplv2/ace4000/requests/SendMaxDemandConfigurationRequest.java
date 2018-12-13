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
public class SendMaxDemandConfigurationRequest extends AbstractConfigMessage {

    public SendMaxDemandConfigurationRequest(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        int register = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1).getValue());
        int numberOfSubIntervals = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.NUMBER_OF_SUBINTERVALS).getValue());
        int subIntervalDuration = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SUB_INTERVAL_DURATION).getValue());

        if (convertToNumberOfSubIntervalsCode(numberOfSubIntervals) == null) {
            failMessage("Max demand configuration message failed, invalid arguments");
            return;
        }
        if (convertToSubIntervalDurationCode(subIntervalDuration) == null) {
            failMessage("Max demand configuration message failed, invalid arguments");
            return;
        }

        trackingId = getAce4000().getObjectFactory().sendMaxDemandConfiguration(register, convertToNumberOfSubIntervalsCode(numberOfSubIntervals), convertToSubIntervalDurationCode(subIntervalDuration));
    }
}