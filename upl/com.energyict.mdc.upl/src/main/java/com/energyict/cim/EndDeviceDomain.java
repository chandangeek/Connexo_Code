/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.cim;

/**
 * Created by IntelliJ IDEA.
 * User: jbr
 * Date: 16-aug-2011
 * Time: 16:25:05
 */
public enum EndDeviceDomain  implements CimMnemonicProvider {
    NOT_APPLICABLE(0, "NotApplicable"),
    COMMUNICATION(1, "Communication"),
    BATTERY(2, "Battery"),
    CARTRIDGE(3, "Cartridge"),
    GASSUPPLY(4, "GasSupply"),
    WATERSUPPLY(5, "WaterSupply"),
    INSTALLATION(6, "Installation"),
    CONFIGURATION(7, "Configuration"),
    DEMAND(8, "Demand"),
    MODULEFIRMWARE(9, "ModuleFirmware"),
    PAIRING(10, "Pairing"),
    FIRMWARE(11, "Firmware"),
    SECURITY(12, "Security"),
    VIDEODISPLAY(13, "VideoDisplay"),
    MOBILESECURITY(14, "MobileSecurity"),
    LOADCONTROL(15, "LoadControl"),
    LOADPROFILE(16, "LoadProfile"),
    LOGS(17, "Logs"),
    MEMORY(18, "Memory"),
    MODEM(19, "Modem"),
    BILLING(20, "Billing"),
    METROLOGY(21, "Metrology"),
    NETWORK(23, "Network"),
    POWER(26, "Power"),
    PRESSURE(29, "Pressure"),
    RCDSWITCH(31, "RCDSwitch"),
    TEMPERATURE(35, "Temperature"),
    CLOCK(36, "Clock"),
    WATCHDOG(37, "Watchdog"),
    KYZPULSECOUNTER(38,"KYZPulseCounter"),
    ASSOCIATEDDEVICE(39,"AssociatedDevice"),
    VOLUME(40,"Volume"),
    RECODER(41,"Recoder"),
    SOME_NOT_STANDARD_DOMAIN1(51, "SNSD1"),
    SOME_NOT_STANDARD_DOMAIN2(52, "SNSD2"),
    SOME_NOT_STANDARD_DOMAIN3(53, "SNSD3"),
    SOME_NOT_STANDARD_DOMAIN4(54, "SNSD4"),
    SOME_NOT_STANDARD_DOMAIN5(55, "SNSD5");

    private int value;
    private String mnemonic;

    private EndDeviceDomain(int value, String mnemonic) {
        this.value = value;
        this.mnemonic = mnemonic;
    }

    public int getValue() {
        return value;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getTranslationKey() {
        return "do" + mnemonic;
    }

    public static EndDeviceDomain fromValue(int value) {
        for (EndDeviceDomain dom : EndDeviceDomain.values()) {
            if (dom.getValue() == value) {
                return dom;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getMnemonic();
    }

}
