/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;


import com.elster.jupiter.issue.share.Priority;

public class IssuePriorityInfoFactory {
    public PriorityInfo asInfo(Priority priority) {
        PriorityInfo info = new PriorityInfo();
        info.urgency = priority.getUrgency();
        info.impact = priority.getImpact();
        return info;
    }

    public int getValue(Priority priority){
        return priority.getUrgency() + priority.getImpact();
    }

}
