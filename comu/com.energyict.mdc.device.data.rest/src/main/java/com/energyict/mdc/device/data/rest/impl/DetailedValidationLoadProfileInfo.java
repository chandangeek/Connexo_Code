/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.LoadProfile;

/**
 * Created by adrianlupan on 4/24/15.
 */
public class DetailedValidationLoadProfileInfo {

    public Long id;
    public String name;
    public Long total;

    public DetailedValidationLoadProfileInfo(LoadProfile loadProfile, Long count) {
        this.id = loadProfile.getId();
        this.name = loadProfile.getLoadProfileSpec().getLoadProfileType().getName();
        this.total = count;
    }

    public DetailedValidationLoadProfileInfo() {

    }
}
