package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@XmlRootElement
public class DeviceMessageCategoryInfo {

    public long id;
    public String name;
    public List<DeviceMessageInfo> deviceMessageEnablements;

    public DeviceMessageCategoryInfo() {
    }

    public static DeviceMessageCategoryInfo from(DeviceMessageCategory category, List<DeviceMessageSpec> messages, List<DeviceMessageEnablement> messageEnablements, Thesaurus thesaurus) {
        DeviceMessageCategoryInfo info = new DeviceMessageCategoryInfo();
        info.id = category.getId();
        info.name = category.getName();
        info.deviceMessageEnablements = new ArrayList<>();

        for (DeviceMessageSpec message : messages) {
            Optional<DeviceMessageEnablement> enablement = messageEnablements.stream().filter(e -> e.getDeviceMessageId() == message.getId()).findAny();
            if (enablement.isPresent()) {
                info.deviceMessageEnablements.add(DeviceMessageInfo.from(message, enablement.get().getUserActions(), thesaurus));
            } else {
                info.deviceMessageEnablements.add(DeviceMessageInfo.from(message));
            }
        }

        return info;
    }
}