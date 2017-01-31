/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.LogBookType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    public ObisCode obisCode;
    public Boolean isInUse;
    public long version;

    public LogBookTypeInfo() {
    }

    public LogBookTypeInfo(LogBookType logbook) {
        this.id = logbook.getId();
        this.name = logbook.getName();
        this.obisCode = logbook.getObisCode();
        this.version = logbook.getVersion();
    }

    public static LogBookTypeInfo from(LogBookType logBookType) {
        return from(logBookType, null);
    }

    public static LogBookTypeInfo from(LogBookType logBookType, Boolean isInUse) {
        LogBookTypeInfo info = new LogBookTypeInfo();
        info.id = logBookType.getId();
        info.name = logBookType.getName();
        info.obisCode = logBookType.getObisCode();
        info.isInUse = isInUse;
        info.version = logBookType.getVersion();
        return info;
    }

    public static List<LogBookTypeInfo> from(List<LogBookType> logBookTypes) {
        List<LogBookTypeInfo> infos = new ArrayList<>(logBookTypes.size());
        for (LogBookType logBookType : logBookTypes) {
            infos.add(LogBookTypeInfo.from(logBookType));
        }
        return infos;
    }
}
