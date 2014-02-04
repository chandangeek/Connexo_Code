package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdw.core.LoadProfileType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class LoadProfileTypeInfo {

    @JsonProperty("name")
    public String name;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @JsonProperty("timeDuration")
    @XmlJavaTypeAdapter(TimeDurationAdapter.class)
    public TimeDuration timeDuration;
    @JsonProperty("inUse")
    public boolean inUse;

    public LoadProfileTypeInfo(LoadProfileType loadProfileType) {
        name = loadProfileType.getName();
        obisCode = loadProfileType.getObisCode();
        timeDuration=loadProfileType.getInterval();
        inUse = loadProfileType.isInUse();
    }
    
}
