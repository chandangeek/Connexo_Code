/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLDeviceMessageCategoryAdapter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Instant;

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

    private DeviceMessageSpecificationService deviceMessageSpecificationService;

    private long id;
    private Reference<ProtocolTask> protocolTask = ValueReference.absent();
    private int deviceMessageCategoryId;
    private DeviceMessageCategory deviceMessageCategory;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    public MessagesTaskTypeUsageImpl() {
    }

    @Inject
    public MessagesTaskTypeUsageImpl(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
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
    @XmlElement(type = UPLDeviceMessageCategoryAdapter.class)
    public DeviceMessageCategory getDeviceMessageCategory() {
        if (this.deviceMessageSpecificationService != null && this.deviceMessageCategory == null) {
            this.deviceMessageCategory = this.deviceMessageSpecificationService.findCategoryById(this.deviceMessageCategoryId).orElse(null);
        }
        return this.deviceMessageCategory;
    }

}