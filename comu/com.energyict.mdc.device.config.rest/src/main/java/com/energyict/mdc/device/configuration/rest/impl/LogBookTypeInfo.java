package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.LogBookType;
import java.util.ArrayList;
import java.util.List;
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

    public LogBookTypeInfo() {
    }

    public static LogBookTypeInfo from(LogBookType logBookType) {
        LogBookTypeInfo logBookTypeInfo = new LogBookTypeInfo();
        logBookTypeInfo.name = logBookType.getName();
        logBookTypeInfo.obisCode = logBookType.getObisCode();
        return logBookTypeInfo;
    }

    public static List<LogBookTypeInfo> from(List<LogBookType> logBookTypes) {
        List<LogBookTypeInfo> infos = new ArrayList<>(logBookTypes.size());
        for (LogBookType logBookType : logBookTypes) {
            infos.add(LogBookTypeInfo.from(logBookType));
        }
        return infos;
    }

}
