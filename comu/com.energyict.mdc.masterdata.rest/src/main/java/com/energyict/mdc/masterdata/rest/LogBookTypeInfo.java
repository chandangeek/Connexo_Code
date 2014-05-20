package com.energyict.mdc.masterdata.rest;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.LogBookType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogBookTypeInfo {
    public Long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obis;;

    public LogBookTypeInfo() {
    }

    public LogBookTypeInfo(LogBookType logbook) {
        this.id = logbook.getId();
        this.name = logbook.getName();
        this.obis = logbook.getObisCode();
    }

    public static LogBookTypeInfo from(LogBookType logBookType) {
        LogBookTypeInfo logBookTypeInfo = new LogBookTypeInfo();
        logBookTypeInfo.id = logBookType.getId();
        logBookTypeInfo.name = logBookType.getName();
        logBookTypeInfo.obis = logBookType.getObisCode();
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
