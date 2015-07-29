package com.energyict.mdc.device.configuration.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

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
