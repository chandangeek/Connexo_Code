package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@XmlRootElement
public class RegisterGroupInfo {
    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("registerTypes")
    public List<RegisterTypeInfo> registerTypes;
    public long version;
}
