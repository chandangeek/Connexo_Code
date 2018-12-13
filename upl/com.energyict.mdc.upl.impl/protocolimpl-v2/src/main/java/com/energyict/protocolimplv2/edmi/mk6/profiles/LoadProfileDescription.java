package com.energyict.protocolimplv2.edmi.mk6.profiles;

import com.energyict.obis.ObisCode;

/**
 * @author sva
 * @since 27/02/2017 - 13:39
 */
public enum LoadProfileDescription {

    LOAD_SURVEY_1_4CHN_30MIN_35Day("Load_survey 1", ObisCode.fromString("1.0.99.1.0.255")),
    LOAD_SURVEY_2_30MIN_15D_VI_V3("Load_Survey 2 30m", ObisCode.fromString("1.0.99.2.0.255")),
    LOAD_SURVEY_2_ABC_AMPS_1MIN("Load_Survey 2 A B C", ObisCode.fromString("1.0.99.3.0.255")),
    MV90_2CH_15MIN_45DAY_NB("MV90 2Ch 15 min 45 day NB", ObisCode.fromString("1.0.99.4.0.255")),
    MV90_2CH_30MIN_90DAY_NB("MV90 2Ch 30 min 90 day NB", ObisCode.fromString("1.0.99.5.0.255")),
    MV90_4CH_15MIN_32DAY_PULSE("MV90 4Ch 15 min 32 day Pulse", ObisCode.fromString("1.0.99.6.0.255")),
    MV90_4CHN_15MIN_35DAY("MV90 4Ch 15min 35 day", ObisCode.fromString("1.0.99.7.0.255")),
    MV90_4CHN_30MIN_35DAY("MV90 4Ch 30 min 35 day", ObisCode.fromString("1.0.99.8.0.255")),
    MV90_4CHN_30MIN_35DAY_PULSE("MV90 4Ch 30 min 45 day Pulse", ObisCode.fromString("1.0.99.9.0.255")),
    UNKNOWN("unknown", ObisCode.fromString("0.0.0.0.0.0"));

    private final String extensionName;
    private final ObisCode profileObisCode;

    LoadProfileDescription(String extensionName, ObisCode profileObisCode) {
        this.extensionName = extensionName;
        this.profileObisCode = profileObisCode;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    public static LoadProfileDescription fromObisCode(ObisCode obisCode) {
        for (LoadProfileDescription loadProfileDescription : values()) {
            if (loadProfileDescription.getProfileObisCode().equals(obisCode)) {
                return loadProfileDescription;
            }
        }
        return UNKNOWN;
    }
}