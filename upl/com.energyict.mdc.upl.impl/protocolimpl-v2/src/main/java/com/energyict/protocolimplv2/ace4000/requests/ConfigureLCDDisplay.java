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
public class ConfigureLCDDisplay extends AbstractConfigMessage {

    public ConfigureLCDDisplay(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        int number1 = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.NUMBER_OF_DIGITS_BEFORE_COMMA).getValue());
        int number2 = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.NUMBER_OF_DIGITS_AFTER_COMMA).getValue());
        String sequence = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.DISPLAY_SEQUENCE).getValue();
        int intervalInSeconds = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.DISPLAY_CYCLE_TIME).getValue());

        int resolutionCode = convertToResolutionCode(number1, number2);
        if (resolutionCode == -1) {
            failMessage("Display configuration request failed, invalid arguments");
            return;
        }

        String[] sequenceCodes = sequence.split(",");
        String sequenceResult = "";
        try {
            for (String code : sequenceCodes) {
                if (code.length() != 1 && code.length() != 2) {
                    failMessage("Display configuration request failed, invalid arguments");
                    return;
                }
                sequenceResult += ((code.length() == 1) ? "0" : "") + code;
            }
        } catch (Exception e) {
            failMessage("Display configuration request failed, invalid arguments");
            return;
        }

        trackingId = getAce4000().getObjectFactory().sendDisplayConfigurationRequest(resolutionCode, sequenceResult, sequence, intervalInSeconds);
    }

    private int convertToResolutionCode(int number1, int number2) {
        if (number1 == 5) {
            switch (number2) {
                case 0:
                    return 5;
                case 1:
                    return 0;
                case 2:
                    return 4;
                case 3:
                    return 3;
            }
        }
        if (number1 == 6) {
            switch (number2) {
                case 0:
                    return 1;
                case 1:
                    return 2;
            }
        }
        if (number1 == 7) {
            switch (number2) {
                case 0:
                    return 6;
                case 1:
                    return 7;
            }
        }
        return -1;
    }
}