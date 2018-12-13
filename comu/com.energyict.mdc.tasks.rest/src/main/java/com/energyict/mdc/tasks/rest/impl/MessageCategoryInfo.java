/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageCategoryInfo {
    public int id;
    public String name;
    public String description;

    public static MessageCategoryInfo from(DeviceMessageCategory category){
        if (category == null){
            throw new IllegalArgumentException("Category can't be null");
        }
        MessageCategoryInfo info = new MessageCategoryInfo();
        info.id = category.getId();
        info.name = category.getName();
        info.description = category.getDescription();
        return info;
    }

    public static List<MessageCategoryInfo> from(List<DeviceMessageCategory> categories){
        if (categories == null){
            throw new IllegalArgumentException("Categories can't be null");
        }
        return categories.stream().map(MessageCategoryInfo::from).collect(Collectors.toList());
    }

    public static Stream<MessageCategoryInfo> from(MessagesTask task){
        if (task == null){
            throw new IllegalArgumentException("MessagesTask can't be null");
        }
        return task.getDeviceMessageCategories().stream().map(MessageCategoryInfo::from);
    }

    public static List<MessageCategoryInfo> fromTasks(List<ProtocolTask> protocolTasks){
        if (protocolTasks == null){
            throw new IllegalArgumentException("ProtocolTasks can't be null");
        }
        return protocolTasks.stream()
                .filter(task -> task instanceof MessagesTask)
                .flatMap(task -> MessageCategoryInfo.from((MessagesTask) task))
                .collect(Collectors.toList());
    }
}
