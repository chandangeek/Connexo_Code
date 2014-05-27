package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.tasks.rest.util.RestHelper;

import java.util.ArrayList;
import java.util.List;

public class ActionInfo {
    public String id;
    public String name;

    public static ActionInfo from(String action) {
        ActionInfo actionInfo = new ActionInfo();
        RestHelper restHelper = new RestHelper();
        actionInfo.id = action;
        actionInfo.name = restHelper.titleize(action);
        return actionInfo;
    }

    public static List<ActionInfo> from(List<String> actions) {
        List<ActionInfo> actionInfos = new ArrayList<>(actions.size());
        for (String action : actions) {
            actionInfos.add(ActionInfo.from(action));
        }
        return actionInfos;
    }
}