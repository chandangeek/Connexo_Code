/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;


import com.elster.jupiter.metering.UsagePoint;

public class UsagePointShortInfoFactory {


    public UsagePointShortInfo asInfo(UsagePoint usagePoint) {
        UsagePointShortInfo info = new UsagePointShortInfo();
        if (usagePoint != null) {
            info.id = usagePoint.getId();
            info.name = usagePoint.getName();
        }
        return info;
    }

}
