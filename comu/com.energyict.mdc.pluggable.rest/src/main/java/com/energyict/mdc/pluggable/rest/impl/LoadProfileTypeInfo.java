/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.masterdata.LoadProfileType;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class LoadProfileTypeInfo  {

    public int loadProfileTypeId;
    public String name;

    public LoadProfileTypeInfo() {
    }

    public LoadProfileTypeInfo(Map<String, Object> map) {
        this.loadProfileTypeId = (int) map.get("loadProfileTypeId");
        this.name = (String) map.get("name");
    }

    public LoadProfileTypeInfo(LoadProfileType loadProfileType) {
        loadProfileTypeId = (int)loadProfileType.getId();
        name = loadProfileType.getName();
    }

}
