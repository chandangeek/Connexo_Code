/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import java.io.Serializable;

public class MeterUsageData implements Serializable {

    private MeterReadingData meterReadingData;
    private ProfileData profileData;

    public MeterUsageData(MeterReadingData meterReadingData, ProfileData profileData) {
        this.meterReadingData = meterReadingData;
        this.profileData = profileData;
    }

    public ProfileData getProfileData() {
        return profileData;
    }

    public void setProfileData(ProfileData profileData) {
        this.profileData = profileData;
    }

    public MeterReadingData getMeterReadingData() {
        return meterReadingData;
    }

    public void setMeterReadingData(MeterReadingData meterReadingData) {
        this.meterReadingData = meterReadingData;
    }


}
