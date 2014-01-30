package com.energyict.mdc.device.configuration.rest.impl;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

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
