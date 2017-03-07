package com.energyict.protocolimplv2.edmi.mk10.profiles;

import com.energyict.obis.ObisCode;

/**
 * @author sva
 * @since 27/02/2017 - 13:39
 */
public enum LoadProfileDescription {

    REGULAR_PROFILE(0, ObisCode.fromString("0.0.99.1.0.255")),
    INSTRUMENTATION_PROFILE(1, ObisCode.fromString("0.0.99.2.0.255")),
    UNKNOWN(-1, ObisCode.fromString("0.0.0.0.0.0"));

    private final int surveyNr;
    private final ObisCode profileObisCode;

    LoadProfileDescription(int surveyNr, ObisCode profileObisCode) {
        this.surveyNr = surveyNr;
        this.profileObisCode = profileObisCode;
    }

    public int getSurveyNr() {
        return surveyNr;
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