package com.energyict.protocolimpl.coronis.core;

import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

import java.util.List;

/**
 * This object is the result of parsing a bubble up frame
 *
 * Copyrights EnergyICT
 * Date: 26-apr-2011
 * Time: 15:30:11
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
