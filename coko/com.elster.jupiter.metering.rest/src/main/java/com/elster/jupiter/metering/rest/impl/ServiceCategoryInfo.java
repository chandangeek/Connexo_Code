/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceCategoryInfo {

    public String name;
    public String displayName;
    public List<IdWithNameInfo> meterRoles;

    public ServiceCategoryInfo(ServiceCategory category) {
        this.name = category.getKind().name();
        this.displayName = category.getName();
        this.meterRoles = category.getMeterRoles().stream().map(this::asInfo).sorted((o1, o2) -> o1.name.compareTo(o2.name)).collect(Collectors.toList());
    }

    private IdWithNameInfo asInfo(MeterRole meterRole) {
        return new IdWithNameInfo(meterRole.getKey(), meterRole.getDisplayName());
    }

}