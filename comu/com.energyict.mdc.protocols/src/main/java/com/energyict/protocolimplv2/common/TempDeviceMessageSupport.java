/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.common;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class TempDeviceMessageSupport implements DeviceMessageSupport {

    private final CollectedDataFactory collectedDataFactory;

    public TempDeviceMessageSupport(CollectedDataFactory collectedDataFactory) {
        super();
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.noneOf(DeviceMessageId.class);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";
    }

}
