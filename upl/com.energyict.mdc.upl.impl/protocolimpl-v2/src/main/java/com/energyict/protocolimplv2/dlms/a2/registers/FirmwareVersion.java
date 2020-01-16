package com.energyict.protocolimplv2.dlms.a2.registers;

import com.energyict.dlms.axrdencoding.OctetString;

public class FirmwareVersion {

    private int version;
    private int commit;
    private int buildDate;
    private int major;
    private int minor;
    private int fix;
    private int year;
    private int month;
    private int day;

    public FirmwareVersion(OctetString octetString) {
        byte[] value = octetString.getOctetStr();
        version = (((((int) value[0]) & 0xFF) << 8) | (((int) value[1]) & 0xFF)) & 0xFFFF;
        commit = (((((int) value[2]) & 0xFF) << 8) | (((int) value[3]) & 0xFF)) & 0xFFFF;
        buildDate = (((((int) value[4]) & 0xFF) << 8) | (((int) value[5]) & 0xFF)) & 0xFFFF;
        major = (version & 0xF800) >> 11;
        minor = (version & 0x07C0) >> 6;
        fix = (version & 0x003F);
        year = 2015 + ((buildDate & 0xFE00) >> 9);
        month = (buildDate & 0x01E0) >> 5;
        day = (buildDate & 0x001F);
    }

    public String getDescription() {
        String versionString = String.join(".", Integer.toString(major), Integer.toString(minor), Integer.toString(fix));
        String date = String.join("-", Integer.toString(year), Integer.toString(month), Integer.toString(day));
        return String.join(" ", "version =", versionString, "\ncommit number =", Integer.toString(commit), "\ndate =", date);
    }

    public int getMinor() {
        return minor;
    }

    public int getMajor() {
        return major;
    }
}
