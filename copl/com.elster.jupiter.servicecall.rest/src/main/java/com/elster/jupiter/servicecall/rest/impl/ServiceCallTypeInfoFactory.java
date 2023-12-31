/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by bvn on 3/2/16.
 */
public class ServiceCallTypeInfoFactory {

    private final Thesaurus thesaurus;
    private static Map<String, String> APPS = ImmutableMap.of("MDC", "MultiSense", "INS", "Insight");

    @Inject
    public ServiceCallTypeInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ServiceCallTypeInfo from(ServiceCallType serviceCallType) {
        ServiceCallTypeInfo info = new ServiceCallTypeInfo();
        info.id = serviceCallType.getId();
        info.version = serviceCallType.getVersion();
        info.name = serviceCallType.getName();
        info.versionName = serviceCallType.getVersionName();
        serviceCallType.getApplication().ifPresent(
                key -> info.reservedByApplication = APPS.get(key));
        info.destination = serviceCallType.getDestinationName();
        info.priority = serviceCallType.getPriority();
        info.status = new IdWithDisplayValueInfo<>(serviceCallType.getStatus().name(), serviceCallType.getStatus()
                .getDisplayName(thesaurus));
        info.logLevel = new IdWithDisplayValueInfo<>(serviceCallType.getLogLevel().name(), serviceCallType.getLogLevel()
                .getDisplayName(thesaurus));
        ServiceCallLifeCycle serviceCallLifeCycle = serviceCallType.getServiceCallLifeCycle();
        info.serviceCallLifeCycle = new IdWithNameInfo(serviceCallLifeCycle.getId(),
                thesaurus.getString(serviceCallLifeCycle.getName(), serviceCallLifeCycle.getName()));
        info.customPropertySets = new ArrayList<>();
        serviceCallType
                .getCustomPropertySets()
                .forEach(cps -> info.customPropertySets.add(new ServiceCallTypeCustomPropertySetInfo(cps)));
        return info;
    }
}
