package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MeterRole;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceCategoryInfo {

    public String name;
    public String displayName;
    public List<String> meterRoles;

    public ServiceCategoryInfo(ServiceCategory category) {
        this.name = category.getKind().name();
        this.displayName = category.getName();
        this.meterRoles = category.getMeterRoles().stream().map(this::getRoleName).sorted().collect(Collectors.toList());
    }

    //Explicit mention of MeterRole interface is needed, otherwise java compiler will erase import of metering.config and
    //bnd tool will not put metering.config in Import-Packages.
    private String getRoleName(MeterRole meterRole) {
        return meterRole.getName();
    }
}
