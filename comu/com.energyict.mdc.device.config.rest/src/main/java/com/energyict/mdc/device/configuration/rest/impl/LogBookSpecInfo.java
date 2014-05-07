package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.LogBookSpec;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogBookSpecInfo extends LogBookTypeInfo{
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;

    public static LogBookTypeInfo from(LogBookSpec logBookSpec) {
        LogBookSpecInfo logBookTypeInfo = new LogBookSpecInfo();
        logBookTypeInfo.id = logBookSpec.getId();
        logBookTypeInfo.name = logBookSpec.getLogBookType().getName();
        logBookTypeInfo.obisCode = logBookSpec.getObisCode();
        logBookTypeInfo.overruledObisCode = logBookSpec.getDeviceObisCode();
        if (logBookTypeInfo.obisCode.equals(logBookTypeInfo.overruledObisCode)){
            logBookTypeInfo.overruledObisCode = null;
        }
        return logBookTypeInfo;
    }
}
