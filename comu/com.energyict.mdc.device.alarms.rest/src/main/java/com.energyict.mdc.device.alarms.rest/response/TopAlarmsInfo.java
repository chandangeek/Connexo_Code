/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.response;


import com.elster.jupiter.issue.share.entity.Issue;

import java.util.List;
import java.util.stream.Collectors;

public class TopAlarmsInfo {

    public long totalUserAssigned;
    public long totalWorkGroupAssigned;
    public long total;
    public List<AlarmInfo> items;

    public TopAlarmsInfo(){

    }

    public TopAlarmsInfo(List<Issue> issues, long totalUserAssigned, long totalWorkGroupAssigned){
        items = issues.stream().map(AlarmInfo::new).collect(Collectors.toList());
        total = items.size();
        this.totalUserAssigned = totalUserAssigned;
        this.totalWorkGroupAssigned = totalWorkGroupAssigned;
    }
}
