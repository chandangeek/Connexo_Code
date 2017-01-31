/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.api.DeviceMessageFile;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class DeviceMessageFileReferenceInfo {

    public long id;
    public String name;

    public DeviceMessageFileReferenceInfo() {
    }

    public DeviceMessageFileReferenceInfo(Map<String, Object> map) {
        this.id = (long) map.get("id");
        this.name = (String) map.get("name");
    }

    public DeviceMessageFileReferenceInfo(DeviceMessageFile deviceMessageFile) {
        id = deviceMessageFile.getId();
        name = deviceMessageFile.getName();
    }

}