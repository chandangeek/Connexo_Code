/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LogBookType;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class LogBookTypeInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public long version;
    public VersionInfo<Long> parent;

    public LogBookTypeInfo() {
    }

    public static LogBookTypeInfo from(LogBookType logBookType) {
        LogBookTypeInfo logBookTypeInfo = new LogBookTypeInfo();
        logBookTypeInfo.id = logBookType.getId();
        logBookTypeInfo.name = logBookType.getName();
        logBookTypeInfo.obisCode = logBookType.getObisCode();
        logBookTypeInfo.version = logBookType.getVersion();
        return logBookTypeInfo;
    }

    public static List<LogBookTypeInfo> from(List<LogBookType> logBookTypes) {
        return from(logBookTypes, null);
    }

    public static List<LogBookTypeInfo> from(List<LogBookType> logBookTypes, DeviceType deviceType) {
        List<LogBookTypeInfo> infos = new ArrayList<>(logBookTypes.size());
        for (LogBookType logBookType : logBookTypes) {
            LogBookTypeInfo info = LogBookTypeInfo.from(logBookType);
            if (deviceType != null) {
                info.parent = new VersionInfo<>(deviceType.getId(), deviceType.getVersion());
            }
            infos.add(info);
        }
        return infos;
    }

}
