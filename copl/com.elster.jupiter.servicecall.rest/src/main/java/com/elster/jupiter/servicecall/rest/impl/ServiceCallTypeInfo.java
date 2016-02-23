package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;

import java.util.ArrayList;
import java.util.List;

public class ServiceCallTypeInfo {
    public long id;
    public long version;
    public String name;
    public String versionName;
    public IdWithDisplayValueInfo<String> status;
    public IdWithDisplayValueInfo<String> logLevel;
    public IdWithNameInfo serviceCallLifeCycle;
    public List<ServiceCallTypeCustomPropertySetInfo> customPropertySets;

    public ServiceCallTypeInfo() {
    }

    public ServiceCallTypeInfo(ServiceCallType serviceCallType, Thesaurus thesaurus) {
        this.id = serviceCallType.getId();
        this.version = serviceCallType.getVersion();
        this.name = serviceCallType.getName();
        this.versionName = serviceCallType.getVersionName();
        this.status = new IdWithDisplayValueInfo<>(serviceCallType.getStatus().name(), serviceCallType.getStatus().getDisplayName(thesaurus));
        this.logLevel = new IdWithDisplayValueInfo<>(serviceCallType.getLogLevel().name(), serviceCallType.getLogLevel().getDisplayName(thesaurus));
        ServiceCallLifeCycle serviceCallLifeCycle = serviceCallType.getServiceCallLifeCycle();
        this.serviceCallLifeCycle = new IdWithNameInfo(serviceCallLifeCycle.getId(), serviceCallLifeCycle.getName());
        customPropertySets = new ArrayList<>();
        serviceCallType.getCustomPropertySets().stream()
                .forEach(cps -> this.customPropertySets.add(new ServiceCallTypeCustomPropertySetInfo(cps)));
        //this.customPropertySets = new ServiceCallTypeCustomPropertySetInfo;
    }

}
