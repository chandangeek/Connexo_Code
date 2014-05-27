package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.tasks.ProtocolTask;

import java.util.ArrayList;
import java.util.List;

public class ProtocolTaskInfo {
    public Long id;
    public String category;
    public String action;
    public List<ParameterInfo> parameters;

    public static ProtocolTaskInfo from(ProtocolTask protocolTask) {
        ProtocolTaskInfo protocolTaskInfo = new ProtocolTaskInfo();
        Categories protocolTaskCategory = getProtocolTaskCategory(protocolTask);
        protocolTaskInfo.id = protocolTask.getId();
        protocolTaskInfo.category = getProtocolTaskCategoryAsStr(protocolTask);
        protocolTaskInfo.action = protocolTaskCategory.getActionAsStr(protocolTaskCategory.getAction(protocolTask));
        protocolTaskInfo.parameters = protocolTaskCategory.getProtocolTaskParameters(protocolTask);
        return protocolTaskInfo;
    }

    public static List<ProtocolTaskInfo> from(List<ProtocolTask> protocolTasks) {
        List<ProtocolTaskInfo> protocolTaskInfos = new ArrayList<>(protocolTasks.size());
        for (ProtocolTask protocolTask : protocolTasks) {
            protocolTaskInfos.add(ProtocolTaskInfo.from(protocolTask));
        }
        return protocolTaskInfos;
    }

    public static String getProtocolTaskCategoryAsStr(ProtocolTask protocolTask) {
        String categoryAsString = null;
        for (Categories category : Categories.values()) {
            if (category.getProtocolTaskClass().isAssignableFrom(protocolTask.getClass())) {
                categoryAsString = category.getId();
                break;
            }
        }
        return categoryAsString;
    }

    public static Categories getProtocolTaskCategory(ProtocolTask protocolTask) {
        Categories protocolTaskCategory = null;
        for (Categories category : Categories.values()) {
            if (category.getProtocolTaskClass().isAssignableFrom(protocolTask.getClass())) {
                protocolTaskCategory = category;
            }
        }
        return protocolTaskCategory;
    }
}