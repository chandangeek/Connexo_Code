package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class ConfigureSpecialDataMode extends AbstractConfigMessage {

    public ConfigureSpecialDataMode(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        String failMsg = "Special data mode configuration message failed, invalid arguments";

        int duration = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DAYS).getValue());
        Date date;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE).getValue());
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }

        int billingEnable = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING).getValue());
        if (billingEnable != 0 && billingEnable != 1) {
            failMessage(failMsg);
            return;
        }
        int billingInterval = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_INTERVAL).getValue());
        if (billingInterval != 0 && billingInterval != 1 && billingInterval != 2) {
            failMessage(failMsg);
            return;
        }
        int billingNumber = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_MAX_NUMBER_RECORDS).getValue());
        if (billingNumber > 0xFFFF || billingNumber < 0) {
            failMessage(failMsg);
            return;
        }

        int loadProfileEnable = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SPECIAL_LOAD_PROFILE).getValue());
        if (loadProfileEnable != 0 && loadProfileEnable != 1) {
            failMessage(failMsg);
            return;
        }
        int loadProfileInterval = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SPECIAL_LOAD_PROFILE_INTERVAL).getValue());
        if (loadProfileInterval != 1 && loadProfileInterval != 2 && loadProfileInterval != 3 && loadProfileInterval != 5 && loadProfileInterval != 6 && loadProfileInterval != 0x0A && loadProfileInterval != 0x0C && loadProfileInterval != 0x0F && loadProfileInterval != 0x14 && loadProfileInterval != 0x1E && loadProfileInterval != 0x3C && loadProfileInterval != 0x78 && loadProfileInterval != 0xF0) {
            failMessage(failMsg);
            return;
        }
        int loadProfileNumber = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SPECIAL_LOAD_PROFILE_MAX_NO).getValue());
        if (loadProfileNumber > 0xFFFF || loadProfileNumber < 0) {
            failMessage(failMsg);
            return;
        }
        trackingId = getAce4000().getObjectFactory().sendSDMConfiguration(billingEnable, billingInterval, billingNumber, loadProfileEnable, loadProfileInterval, loadProfileNumber, duration, date);
    }
}