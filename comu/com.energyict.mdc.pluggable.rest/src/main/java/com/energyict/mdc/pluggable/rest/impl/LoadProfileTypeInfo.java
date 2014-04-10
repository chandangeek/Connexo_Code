package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.device.config.LoadProfileType;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

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
