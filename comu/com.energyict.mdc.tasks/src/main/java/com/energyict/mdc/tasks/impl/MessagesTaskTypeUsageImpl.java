package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageService;
import com.energyict.mdc.tasks.ProtocolTask;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.inject.Inject;

/**
 * Provides an implementation for the {@link MessagesTaskTypeUsage} interface.
 *
 * Copyrights EnergyICT
 * Date: 26/02/13
 * Time: 14:38
 */
public class MessagesTaskTypeUsageImpl implements MessagesTaskTypeUsage {

    enum Fields {
        PROTOCOL_TASK("protocolTask"),
        DEVICE_MESSAGE_CATEGORY("deviceMessageCategoryId");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }

    }

    private final DeviceMessageService deviceMessageService;

    private long id;
    private Reference<ProtocolTask> protocolTask = ValueReference.absent();
    private int deviceMessageCategoryId;
    private DeviceMessageCategory deviceMessageCategory;

    @Inject
    public MessagesTaskTypeUsageImpl(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    MessagesTaskTypeUsageImpl initialize(ProtocolTask protocolTask, DeviceMessageCategory category) {
        this.protocolTask.set(protocolTask);
        this.deviceMessageCategoryId = category.getId();
        this.deviceMessageCategory = category;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public DeviceMessageCategory getDeviceMessageCategory() {
        if (this.deviceMessageCategory == null) {
            this.deviceMessageCategory = this.deviceMessageService.findCategoryById(this.deviceMessageCategoryId).orElse(null);
        }
        return this.deviceMessageCategory;
    }

}