package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdw.core.LoadProfileType;
import com.energyict.mdw.core.LogBookType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class LogBookTypeInfo {

    @JsonProperty("name")
    public String name;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;

    public LogBookTypeInfo(LogBookType logBookType) {
        name = logBookType.getName();
        obisCode = logBookType.getObisCode();
    }
    
}
