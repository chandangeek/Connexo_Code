package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.api.DeviceMessageFile;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Represents the Info object for a {@link DeviceMessageFile}
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:08
 */
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