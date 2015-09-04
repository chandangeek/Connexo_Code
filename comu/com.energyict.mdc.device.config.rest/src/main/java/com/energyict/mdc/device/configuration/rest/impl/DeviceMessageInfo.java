package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import com.elster.jupiter.nls.Thesaurus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@XmlRootElement
public class DeviceMessageInfo {

    public long id;
    public String name;
    public boolean active;
    public List<DeviceMessagePrivilegeInfo> privileges;

    public DeviceMessageInfo() {
    }

    public static DeviceMessageInfo from(DeviceMessageSpec messageSpec) {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.id = messageSpec.getId().dbValue();
        info.name = messageSpec.getName();
        info.active = false;
        return info;
    }

    public static DeviceMessageInfo from(DeviceMessageSpec messageSpec, Set<DeviceMessageUserAction> userActions, Thesaurus thesaurus) {
        DeviceMessageInfo info = from(messageSpec);
        info.active = true;
        info.privileges = userActions.stream()
                .map(p -> DeviceMessagePrivilegeInfo.from(p, thesaurus))
                .sorted((p1, p2) -> p1.name.compareTo(p2.name))
                .collect(Collectors.toList());
        return info;
    }

}