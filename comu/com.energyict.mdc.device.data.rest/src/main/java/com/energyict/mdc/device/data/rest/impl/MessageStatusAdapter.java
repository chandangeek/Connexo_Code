package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.util.stream.Stream;

/**
 * Created by bvn on 10/22/14.
 */
public class MessageStatusAdapter extends MapBasedXmlAdapter<DeviceMessageStatus> {

    public MessageStatusAdapter() {
        register("", null);
        Stream.of(DeviceMessageStatusTranslationKeys.values())
            .map(DeviceMessageStatusTranslationKeys::getDeviceMessageStatus)
            .forEach(this::register);
    }

    private void register(DeviceMessageStatus deviceMessageStatus) {
        this.register(deviceMessageStatus.name(), deviceMessageStatus);
    }

}