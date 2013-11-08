/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * @author jme
 */
public class FirmwareVersions extends Data {

    public static final String FW1_22 = "1.22";
    public static final String FW1_27 = "1.27";

    private static final int ATTRB_FW_VERSION = 0x08;

    private FirmwareVersionAttribute firmwareVersionAttribute;

    protected FirmwareVersions() {
        super(null, null);

    }

    public FirmwareVersions(ObisCode obisCode, AS220 as220) throws IOException {
        super(as220.getCosemObjectFactory().getProtocolLink(), as220.getCosemObjectFactory().getObjectReference(obisCode));
    }

    /**
     * @return
     */
    public FirmwareVersionAttribute getFirmwareVersion() {
        if (firmwareVersionAttribute == null) {
            try {
                firmwareVersionAttribute = new FirmwareVersionAttribute(getResponseData(ATTRB_FW_VERSION));
            } catch (IOException e) {
                //Absorb;
            }
        }
        return firmwareVersionAttribute;
    }

    /**
     * Checks if the given version matches the version returned by getVersion()
     *
     * @param firmwareVersion
     * @return
     */
    public boolean isVersion(String firmwareVersion) {
        if (getFirmwareVersion() != null) {
            return getFirmwareVersion().getVersion().equalsIgnoreCase(firmwareVersion);
        }
        return false;
    }

    /**
     * Check if the firmwareVersion == version 1.22
     * @return
     */
    public boolean is122() {
        return isVersion(FW1_22);
    }

    /**
     * Check if the firmwareVersion == version 1.27
     * @return
     */
    public boolean is127() {
        return isVersion(FW1_27);
    }

    @Override
    public String toString() {
        FirmwareVersionAttribute fw = getFirmwareVersion();
        return (fw != null) ? fw.getVersion() : "Unknown";
    }

    /**
     * Check if the current firmwareVersion is higher or equals to the provided version
     * @param v The version to check with (format : majorVersion.minorVersion , minorversion is not required)
     * @return
     */
    public boolean isHigherOrEqualsThen(String v) {
        int majorVersion = -1;
        int minorVersion = -1;
        int currentMajorVersion = getFirmwareVersion().getMajorVersion();

        if (v.contains(".")) {
            String tempVersion[] = v.split("\\.");
            majorVersion = Integer.valueOf(tempVersion[0]);
            minorVersion = Integer.valueOf(tempVersion[1]);

            if (currentMajorVersion > majorVersion) {
                return true;
            } else if (currentMajorVersion == majorVersion) {
                int currentMinorversion = getFirmwareVersion().getMinorVersion();
                if (currentMinorversion >= minorVersion) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        } else {
            majorVersion = Integer.valueOf(v);
            return currentMajorVersion >= majorVersion;
        }

    }

    protected void setFirmwareVersionAttribute(FirmwareVersionAttribute fva) {
        this.firmwareVersionAttribute = fva;
    }

}
