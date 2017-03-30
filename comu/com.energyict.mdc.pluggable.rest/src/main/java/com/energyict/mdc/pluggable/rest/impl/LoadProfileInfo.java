/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.device.data.LoadProfile;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class LoadProfileInfo  {

    public int id;
    public String name;

    public LoadProfileInfo() {
    }

    public LoadProfileInfo(Map<String, Object> map) {
        this.id = (int) map.get("id");
        this.name = (String) map.get("name");
    }

    public LoadProfileInfo(LoadProfile loadProfile) {
        id = (int)loadProfile.getId();
        name = loadProfile.getLoadProfileSpec().getLoadProfileType().getName();
    }

}
