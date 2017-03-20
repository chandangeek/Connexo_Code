package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.Collections;
import java.util.List;

/**
 * Test enum implementing DeviceMessageSpec
 * <p>
 * Copyrights EnergyICT
 * Date: 8/02/13
 * Time: 15:16
 */
public enum DeviceMessageTestSpecContactor implements DeviceMessageSpec {

    CONTACTOR_OPEN_WITH_OUTPUT(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT),
    CONTACTOR_CLOSE_WITH_OUTPUT(DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT),
    CONTACTOR_ARM(DeviceMessageId.CONTACTOR_ARM),
    CONTACTOR_CLOSE(DeviceMessageId.CONTACTOR_CLOSE),
    CONTACTOR_OPEN(DeviceMessageId.CONTACTOR_OPEN);

    private final DeviceMessageId id;

    DeviceMessageTestSpecContactor(DeviceMessageId id) {
        this.id = id;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageTestCategories.CONTACTOR;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public DeviceMessageId getId() {
        return id;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }
}