/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

public class ActionInfo {

    private static final ActionAdapter actionAdapter = new ActionAdapter();
    @XmlJavaTypeAdapter(ActionAdapter.class)

    public String id;
    public String name;

    public static ActionInfo from(String action, Thesaurus thesaurus) {
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.id = action;
        String key = actionAdapter.marshal(action);
        actionInfo.name = thesaurus.getString(key, key);
        return actionInfo;
    }

    public static List<ActionInfo> from(List<String> actions, Thesaurus thesaurus) {
        List<ActionInfo> actionInfos = new ArrayList<>(actions.size());
        for (String action : actions) {
            actionInfos.add(ActionInfo.from(action, thesaurus));
        }
        return actionInfos;
    }
}