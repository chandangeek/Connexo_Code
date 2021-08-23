package com.energyict.protocolimplv2.umi.ei4.structures;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.nio.charset.StandardCharsets;

/**
 * Contains the version number of the currently active software image.
 * Software version numbers are of the form Major, Minor, Bugfix
 * and also have a CRC and human-readable string containing additional information.
 */
public class UmiActiveSoftwareVersion extends LittleEndianData {
    public static final int FIXED_SIZE = 6;
    public static final UmiCode ACTIVE_SOFTWARE_VERSION_UMI_CODE = new UmiCode("umi.1.1.0.2");
    public static final String ACTIVE_HOST_SOFTWARE_VERSION = "1.23.28";

    /**
     * The major part of the version number (binary coded).
     */
    private int major;     // 1
    /**
     * The minor part of the version number (binary coded).
     */
    private int minor;     // 1
    /**
     * The bugfix part of the version number (binary coded).
     */
    private int bugfix;    // 1
    /**
     * Firmware slot at which this version is stored.
     */
    private int unit;      // 1
    /**
     * The CRC of the software image.
     */
    private int crc;        // 2
    /**
     * An ASCII coded string, containing additional information about the software. Further format details are specified in the SW Release Guide document.
     */
    private String details; // 65 - max length

    public UmiActiveSoftwareVersion(byte[] raw) {
        super(raw, raw.length, false);
        this.major = Byte.toUnsignedInt(getRawBuffer().get());
        this.minor = Byte.toUnsignedInt(getRawBuffer().get());
        this.bugfix = Byte.toUnsignedInt(getRawBuffer().get());
        this.unit = Byte.toUnsignedInt(getRawBuffer().get());
        this.crc = Short.toUnsignedInt(getRawBuffer().getShort());
        byte[] detailsBytes = new byte[raw.length-FIXED_SIZE];
        getRawBuffer().get(detailsBytes);
        this.details = String.copyValueOf(UmiHelper.convertBytesToChars(detailsBytes)).trim();
    }

    /**
     * Constructor for testing purposes
     */
    public UmiActiveSoftwareVersion(byte major, byte minor, byte bugfix, byte unit, int crc, String details) {
        super(FIXED_SIZE + details.length());
        this.major = major;
        this.minor = minor;
        this.bugfix = bugfix;
        this.unit = unit;
        this.crc = crc;
        this.details = details;

        getRawBuffer().put((byte) this.major)
                .put((byte) this.minor)
                .put((byte) this.bugfix)
                .put((byte) this.unit)
                .putShort((short) this.crc)
                .put(details.getBytes(StandardCharsets.US_ASCII));
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getBugfix() {
        return bugfix;
    }

    public int getUnit() {
        return unit;
    }

    public int getCrc() {
        return crc;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(major).append(".").append(minor).append(".").append(bugfix);
        return stringBuilder.toString();
    }

}
