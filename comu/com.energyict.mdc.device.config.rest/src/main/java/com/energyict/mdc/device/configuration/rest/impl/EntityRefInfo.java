package com.energyict.mdc.device.configuration.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown=true)
public class EntityRefInfo {

    public long id;
    public long version;

    public EntityRefInfo() {}
    
    public EntityRefInfo(long id, long version) {
        this.id = id;
        this.version = version;
    }
}
