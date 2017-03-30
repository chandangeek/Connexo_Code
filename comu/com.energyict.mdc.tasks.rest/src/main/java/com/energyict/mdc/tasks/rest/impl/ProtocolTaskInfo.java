/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.rest.Categories;

import java.util.ArrayList;
import java.util.List;

public class ProtocolTaskInfo {
    public Long id;
    public String category;
    public String categoryId;
    public String action;
    public String actionId;
    public List<ParameterInfo> parameters;

    public static ProtocolTaskInfo from(ProtocolTask protocolTask, Thesaurus thesaurus) {
        ProtocolTaskInfo protocolTaskInfo = null;
        Categories protocolTaskCategory = getProtocolTaskCategory(protocolTask);
        if (protocolTaskCategory != null) {
            protocolTaskInfo = new ProtocolTaskInfo();
            protocolTaskInfo.id = protocolTask.getId();
            protocolTaskInfo.category = thesaurus.getString(protocolTaskCategory.getId(),protocolTaskCategory.getId());
            protocolTaskInfo.categoryId = protocolTaskCategory.getId();
            String actionString = protocolTaskCategory.getActionAsStr(protocolTaskCategory.getAction(protocolTask));
            protocolTaskInfo.actionId = actionString;
            protocolTaskInfo.action = thesaurus.getString(actionString,actionString);
            protocolTaskInfo.parameters = protocolTaskCategory.getProtocolTaskParameters(protocolTask);
        }
        return protocolTaskInfo;
    }

    public static List<ProtocolTaskInfo> from(List<ProtocolTask> protocolTasks, Thesaurus thesaurus) {
        List<ProtocolTaskInfo> protocolTaskInfos = new ArrayList<>(protocolTasks.size());
        for (ProtocolTask protocolTask : protocolTasks) {
            ProtocolTaskInfo info = ProtocolTaskInfo.from(protocolTask, thesaurus);
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