package com.energyict.mdc.tasks.rest.impl.infos;

import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.rest.impl.Categories;

import java.util.ArrayList;
import java.util.List;

public class ProtocolTaskInfo {
    private Long id;
    private String category;
    private String action;
    private List<ParameterInfo> parameters;

    public static ProtocolTaskInfo from(ProtocolTask protocolTask) {
        ProtocolTaskInfo protocolTaskInfo = new ProtocolTaskInfo();
        Categories protocolTaskCategory = getProtocolTaskCategory(protocolTask);
        protocolTaskInfo.setId(protocolTask.getId());
        protocolTaskInfo.setCategory(getProtocolTaskCategoryAsStr(protocolTask));
        protocolTaskInfo.setAction(protocolTaskCategory.getActionAsStr(protocolTaskCategory.getAction(protocolTask)));
        protocolTaskInfo.setParameters(protocolTaskCategory.getProtocolTaskParameters(protocolTask));
        return protocolTaskInfo;
    }

    public static List<ProtocolTaskInfo> from(List<ProtocolTask> protocolTasks) {
        List<ProtocolTaskInfo> protocolTaskInfos = new ArrayList<>(protocolTasks.size());
        for (ProtocolTask protocolTask : protocolTasks) {
            protocolTaskInfos.add(ProtocolTaskInfo.from(protocolTask));
        }
        return protocolTaskInfos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
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