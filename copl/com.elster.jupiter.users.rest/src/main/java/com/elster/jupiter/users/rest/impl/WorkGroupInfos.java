/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.WorkGroupInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class WorkGroupInfos {

    public int total;
    public List<WorkGroupInfo> workGroups = new ArrayList<>();

    public WorkGroupInfos() {
    }

    public WorkGroupInfos(WorkGroup workGroup) {
        this();
        add(workGroup);
    }

    public WorkGroupInfos(Iterable<? extends WorkGroup> workGroups) {
        this();
        addAll(workGroups);
    }

    public WorkGroupInfo add(WorkGroup workGroup) {
        WorkGroupInfo result = new WorkGroupInfo(workGroup);
        workGroups.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends WorkGroup> workGroup) {
        for (WorkGroup each : workGroup) {
            add(each);
        }
    }
}

