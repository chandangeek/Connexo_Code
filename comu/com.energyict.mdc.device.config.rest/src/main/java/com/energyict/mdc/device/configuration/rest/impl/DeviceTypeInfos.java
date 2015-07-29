package com.energyict.mdc.device.configuration.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
@JsonPropertyOrder({"total", "deviceTypes"})
public class DeviceTypeInfos {
    @JsonIgnore
    private int couldHaveNextPage = 0;

    @JsonProperty("deviceTypes")
    public List<DeviceTypeInfo> deviceTypes = new ArrayList<>();

    @JsonProperty("total")
    public int getTotal() {
        return deviceTypes.size() + couldHaveNextPage;
    }

    public void setCouldHaveNextPage(){
        couldHaveNextPage = 1;
    }
}
