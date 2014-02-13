package com.energyict.mdc.rest.impl;

import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.rest.impl.properties.MdcResourceProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class LoadProfileTypeInfo implements MdcResourceProperty {

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
