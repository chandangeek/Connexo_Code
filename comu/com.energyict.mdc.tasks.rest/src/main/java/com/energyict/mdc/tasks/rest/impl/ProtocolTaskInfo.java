package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.tasks.ProtocolTask;

import com.energyict.mdc.tasks.rest.Categories;
import java.util.ArrayList;
import java.util.List;

public class ProtocolTaskInfo {
    public Long id;
    public String category;
    public String action;
    public List<ParameterInfo> parameters;

    public static ProtocolTaskInfo from(ProtocolTask protocolTask) {
        ProtocolTaskInfo protocolTaskInfo = null;
        Categories protocolTaskCategory = getProtocolTaskCategory(protocolTask);
        if (protocolTaskCategory != null) {
            protocolTaskInfo = new ProtocolTaskInfo();
            protocolTaskInfo.id = protocolTask.getId();
            protocolTaskInfo.category = protocolTaskCategory.getId();
            protocolTaskInfo.action = protocolTaskCategory.getActionAsStr(protocolTaskCategory.getAction(protocolTask));
            protocolTaskInfo.parameters = protocolTaskCategory.getProtocolTaskParameters(protocolTask);
        }
        return protocolTaskInfo;
    }

    public static List<ProtocolTaskInfo> from(List<ProtocolTask> protocolTasks) {
        List<ProtocolTaskInfo> protocolTaskInfos = new ArrayList<>(protocolTasks.size());
        for (ProtocolTask protocolTask : protocolTasks) {
            ProtocolTaskInfo info = ProtocolTaskInfo.from(protocolTask);
            if (info != null) {
                protocolTaskInfos.add(info);
            }
        }
        return protocolTaskInfos;
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