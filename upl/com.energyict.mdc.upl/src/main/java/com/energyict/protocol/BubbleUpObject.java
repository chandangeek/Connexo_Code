/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol;

import java.util.List;

public class BubbleUpObject {

    private List<RegisterValue> registerValues = null;
    private List<ProfileData> profileDatas = null;

    public List<ProfileData> getProfileDatas() {
        return profileDatas;
    }

    public void setProfileDatas(List<ProfileData> profileDatas) {
        this.profileDatas = profileDatas;
    }

    public List<RegisterValue> getRegisterValues() {
        return registerValues;
    }

    public void setRegisterValues(List<RegisterValue> registerValues) {
        this.registerValues = registerValues;
    }
}