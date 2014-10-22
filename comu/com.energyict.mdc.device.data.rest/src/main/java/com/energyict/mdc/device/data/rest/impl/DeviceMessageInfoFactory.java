package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceMessageInfoFactory {

    public DeviceMessageInfo asInfo(DeviceMessage<?> deviceMessage) {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.id = deviceMessage.getId();
        info.trackingId = deviceMessage.getTrackingId();
        info.name = deviceMessage.getSpecification().getName();
        info.category = deviceMessage.getSpecification().getCategory().getName();
        info.status = deviceMessage.getStatus();
        info.creationDate = deviceMessage.getCreationDate();
        info.releaseDate = deviceMessage.getReleaseDate();
        info.sentDate = deviceMessage.getSentDate().orElse(null);
        info.user = deviceMessage.getUser().getName();
        info.errorMessage = deviceMessage.getProtocolInfo();
        return info;
    }
}
