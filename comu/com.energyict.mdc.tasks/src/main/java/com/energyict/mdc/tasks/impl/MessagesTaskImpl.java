/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.tasks.MessagesTask;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.PENDING_MESSAGES_FLAG;
import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.SENT_MESSAGES_FLAG;
import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;

/**
 * Implementation of a {@link com.energyict.mdc.tasks.MessagesTask}.
 *
 * @author gna
 * @since 2/05/12 - 13:39
 */
class MessagesTaskImpl extends ProtocolTaskImpl implements MessagesTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags(PENDING_MESSAGES_FLAG, SENT_MESSAGES_FLAG, SLAVE_DEVICES_FLAG);

    enum Fields {
        DEVICE_MESSAGE_USAGES("deviceMessageUsages"),
        MESSAGE_TASK_TYPE("messageTaskType");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private List<MessagesTaskTypeUsage> deviceMessageUsages = new ArrayList<>();
    private MessageTaskType messageTaskType = MessageTaskType.NONE;

    @Inject
    MessagesTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    public List<DeviceMessageCategory> getDeviceMessageCategories() {
        return this.deviceMessageUsages.stream().map(MessagesTaskTypeUsage::getDeviceMessageCategory).collect(Collectors.toList());
    }

    public void setDeviceMessageCategories(List<DeviceMessageCategory> deviceMessageCategories) {
        this.deviceMessageUsages.clear();   // Delete all
        for (DeviceMessageCategory category : deviceMessageCategories) {
            MessagesTaskTypeUsageImpl messagesTaskTypeUsage = this.getDataModel().getInstance(MessagesTaskTypeUsageImpl.class).initialize(this, category);
            this.deviceMessageUsages.add(messagesTaskTypeUsage);
        }
        if (!deviceMessageUsages.isEmpty()) {
            this.messageTaskType = MessageTaskType.SELECTED;
        }
    }

    @Override
    public MessageTaskType getMessageTaskType() {
        return this.messageTaskType;
    }

    public void setMessageTaskType(MessageTaskType messageTaskType) {
        this.messageTaskType = messageTaskType;
        if(this.messageTaskType.equals(MessageTaskType.ALL) || this.messageTaskType.equals(MessageTaskType.NONE)){
            this.deviceMessageUsages.clear();
        }
    }

    void deleteDependents() {
        this.deviceMessageUsages.clear();
    }

    public void toJournalMessageDescription(DescriptionBuilder builder) {
        PropertyDescriptionBuilder messageCategoriesBuilder = builder.addListProperty("messageCategories");
        for (DeviceMessageCategory deviceMessageCategory : this.getDeviceMessageCategories()) {
            messageCategoriesBuilder = messageCategoriesBuilder.append(deviceMessageCategory.getName()).next();
        }
    }

}