package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

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

}
