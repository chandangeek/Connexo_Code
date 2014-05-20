package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.tasks.rest.util.RestHelper;

import java.util.ArrayList;
import java.util.List;

public class ActionInfo {
    private String id;
    private String name;

    public static ActionInfo from(String action) {
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setId(action);
        actionInfo.setName(RestHelper.capitalize(action));
        return actionInfo;
    }

    public static List<ActionInfo> from(List<String> actions) {
        List<ActionInfo> actionInfos = new ArrayList<>(actions.size());
        for (String action : actions) {
            actionInfos.add(ActionInfo.from(action));
        }
        return actionInfos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}