/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.util.stream.Stream;

/**
 * Created by bvn on 10/22/14.
 */
public class MessageStatusAdapter extends MapBasedXmlAdapter<DeviceMessageStatus> {

    public MessageStatusAdapter() {
        Stream.of(DeviceMessageStatusTranslationKeys.values())
            .forEach(key->register(key.getDefaultFormat(), key.getDeviceMessageStatus()));
    }
}