package com.energyict.mdc.protocol.api;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 31-mei-2011
 * Time: 9:12:27
 */
public class BubbleUpObject {

    private List<RegisterValue> registerValues = null;
    private List<ProfileData> profileDatas = null;

    public void setProfileDatas(List<ProfileData> profileDatas) {
        this.profileDatas = profileDatas;
    }

    public void setRegisterValues(List<RegisterValue> registerValues) {
        this.registerValues = registerValues;
    }

    public List<ProfileData> getProfileDatas() {
        return profileDatas;
    }

    public List<RegisterValue> getRegisterValues() {
        return registerValues;
    }
}