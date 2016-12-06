package com.energyict.mdc.protocol.api.device.data;

import java.io.Serializable;
import com.energyict.protocol.ProfileData;

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
