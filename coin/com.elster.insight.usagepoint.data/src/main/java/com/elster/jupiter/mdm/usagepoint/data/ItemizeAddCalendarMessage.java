/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ItemizeAddCalendarMessage {

    private Action action;
    private List<String> usagePointMRIDs;
    private List<Long> calendarIds;
    private UsagePointFilter usagePointFilter;
    private long startTime;
    private boolean immediately;

    public boolean isImmediately() {
        return immediately;
    }

    public void setImmediately(boolean immediately) {
        this.immediately = immediately;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<String> getUsagePointMRIDs() {
        return usagePointMRIDs;
    }

    public void setUsagePointMRIDs(List<String> usagePointMRIDs) {
        if (usagePointMRIDs == null) {
            this.usagePointMRIDs = null;
            return;
        }
        this.usagePointMRIDs = ImmutableList.copyOf(usagePointMRIDs);
    }

    public List<Long> getCalendarIds() {
        return calendarIds;
    }

    public void setCalendarIds(List<Long> calendarIds) {
        if (calendarIds == null) {
            this.calendarIds = null;
            return;
        }
        this.calendarIds = ImmutableList.copyOf(calendarIds);
    }

    public UsagePointFilter getUsagePointFilter() {
        return usagePointFilter;
    }

    public void setUsagePointFilter(UsagePointFilter usagePointFilter) {
        this.usagePointFilter = usagePointFilter;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

}
