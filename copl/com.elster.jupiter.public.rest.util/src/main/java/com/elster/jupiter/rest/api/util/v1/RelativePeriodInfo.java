/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1;

import com.elster.jupiter.time.RelativePeriod;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public class RelativePeriodInfo {
    public Long id;
    public String name;
    public RelativeDateInfo from;
    public RelativeDateInfo to;
    public long version;

    public RelativePeriodInfo() {
    }

    public static RelativePeriodInfo from(RelativePeriod relativePeriod) {
        RelativePeriodInfo info = new RelativePeriodInfo();
        info.id = relativePeriod.getId();
        info.name = relativePeriod.getName();
        info.version = relativePeriod.getVersion();
        return info;
    }
}