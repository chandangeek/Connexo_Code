package com.energyict.protocolimpl.dlms.g3;

import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 26/03/12
 * Time: 9:11
 */
public enum G3ProfileType {

    IMPORT_ACTIVE_POWER_PROFILE(1, ObisCode.fromString("1.1.99.1.0.255")),
    EXPORT_ACTIVE_POWER_PROFILE(2, ObisCode.fromString("1.2.99.1.0.255")),
    DAILY_PROFILE(3, ObisCode.fromString("1.0.98.1.2.255")),
    MONTHLY_PROFILE(4, ObisCode.fromString("1.0.98.1.1.255"));

    private final int profileId;
    private final ObisCode obisCode;

    private G3ProfileType(int profileId, ObisCode obisCode) {
        this.profileId = profileId;
        this.obisCode = obisCode;
    }

    public static G3ProfileType fromProfileId(final int profileId) {
        for (final G3ProfileType profileType : values()) {
            if (profileType.profileId == profileId) {
                return profileType;
            }
        }
        throw new IllegalArgumentException("Unknown profile ID [" + profileId + "]. Correct value should be [1 for A+, 2 for A-, 3 for daily profile or 4 for monthly profile].");
    }

    public static G3ProfileType fromObisCode(ObisCode obisCode) {
        for (final G3ProfileType profileType : values()) {
            if (profileType.obisCode.equals(obisCode)) {
                return profileType;
            }
        }
        throw new IllegalArgumentException("Unknown profile ObisCode [" + obisCode + "].");
    }

    public int getProfileId() {
        return profileId;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public boolean isDaily() {
        return this == DAILY_PROFILE;
    }

    public boolean isMonthly() {
        return this == MONTHLY_PROFILE;
    }
}