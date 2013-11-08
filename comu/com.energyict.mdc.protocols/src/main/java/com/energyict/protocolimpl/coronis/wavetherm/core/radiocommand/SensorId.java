package com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 28-mrt-2011
 * Time: 9:42:45
 */
public class SensorId {

    private int familyCode;
    private String serialNumber;

    public SensorId(int familyCode, String serialNumber) {
        this.familyCode = familyCode;
        this.serialNumber = serialNumber;
    }

    public SensorId(int familyCode, byte[] serialNumber) {
        this.familyCode = familyCode;
        this.serialNumber = ProtocolTools.getHexStringFromBytes(serialNumber, "");
    }

    public int getFamilyCode() {
        return familyCode;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
