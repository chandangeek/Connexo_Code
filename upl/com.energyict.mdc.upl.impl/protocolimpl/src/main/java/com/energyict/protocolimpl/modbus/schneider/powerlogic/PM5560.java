package com.energyict.protocolimpl.modbus.schneider.powerlogic;

import com.energyict.protocolimpl.modbus.generic.Generic;

import java.io.IOException;

/**
 * @author sva
 * @since 18/03/2015 - 11:24
 */
public class PM5560 extends Generic {

    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }

//    @Override - date/time currently out of scope
//    public Date getTime() throws IOException {
//        AbstractRegister dateTimeRegister = getRegisterFactory().findRegister(PM5560RegisterFactory.CurrentDateTimeAddress);
//        return ((DateTime) dateTimeRegister.value()).getMeterCalender().getTime();
//    }

    @Override
    public String getFirmwareVersion() throws IOException {
        String rawOsFirmwareVersion = getRegisterFactory().findRegister(PM5560RegisterFactory.OsFirmwareVersionAddress).value().toString();
        String rawRsFirmwareVersion = getRegisterFactory().findRegister(PM5560RegisterFactory.RsFirmwareVersionAddress).value().toString();
        return "OS version: " + formatFirmwareVersion(rawOsFirmwareVersion) + ", RS version: " + formatFirmwareVersion(rawRsFirmwareVersion);
    }

    private String formatFirmwareVersion(String version) {
        version = leftAppendWithZeros(version);
        return version.substring(0, 2) + "." + version.substring(2, 4) + "." + version.substring(4);
    }

    private String leftAppendWithZeros(String text) {
        while (text.length() < 5) {
            text = "0".concat(text);
        }
        return text;
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new PM5560RegisterFactory(this));
    }
}