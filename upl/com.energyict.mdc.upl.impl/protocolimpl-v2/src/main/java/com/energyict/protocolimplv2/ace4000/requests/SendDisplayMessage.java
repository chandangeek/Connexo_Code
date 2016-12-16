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
public class SendDisplayMessage extends AbstractConfigMessage {

    private final int mode;

    public SendDisplayMessage(ACE4000Outbound ace4000, int mode) {
        super(ace4000);
        this.mode = mode;
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        String message = "";
        if (mode == 1) {
            message = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SHORT_DISPLAY_MESSAGE).getValue();
            if (message.length() > 8) {
                failMessage("Display message failed, invalid arguments");
                return;
            }
        } else if (mode == 2) {
            message = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.LONG_DISPLAY_MESSAGE).getValue();
            if (message.length() > 1024) {
                failMessage("Display message failed, invalid arguments");
                return;
            }
        }

        trackingId = getAce4000().getObjectFactory().sendDisplayMessage(mode, message);
    }
}