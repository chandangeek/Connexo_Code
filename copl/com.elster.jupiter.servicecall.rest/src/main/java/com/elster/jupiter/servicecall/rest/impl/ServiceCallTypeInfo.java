package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.Status;
import java.util.Optional;

public class ServiceCallTypeInfo {
    public long id;
    public long version;
    public String name;
    public String versionName;
    public Status status;
    private LogLevel logLevel;
    public IdWithNameInfo serviceCallLifeCycle;

    public ServiceCallTypeInfo() {
    }

    public ServiceCallTypeInfo(ServiceCallType serviceCallType) {
        this.id = serviceCallType.getId();
        this.version = serviceCallType.getVersion();
        this.name = serviceCallType.getName();
        this.versionName = serviceCallType.getVersionName();
        this.status = serviceCallType.getStatus();
        this.logLevel = serviceCallType.getLogLevel();
        Optional<ServiceCallLifeCycle> serviceCallLifeCycle = serviceCallType.getServiceCallLifeCycle();
        this.serviceCallLifeCycle = serviceCallLifeCycle.isPresent() ?
                new IdWithNameInfo(serviceCallLifeCycle.get().getId(), serviceCallLifeCycle.get().getName()) : null;
    }

}
