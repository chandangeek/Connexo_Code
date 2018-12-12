/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.servicecall.InvalidPropertySetDomainTypeException;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.ServiceCallTypeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class ServiceCallTypeBuilderImpl implements ServiceCallTypeBuilder {
    private final ServiceCallTypeImpl instance;
    private final DataModel dataModel;
    private final List<RegisteredCustomPropertySet> toBeRegisteredCustomPropertySets = new ArrayList<>();
    private final Thesaurus thesaurus;

    public ServiceCallTypeBuilderImpl(IServiceCallService serviceCallService, String name, String versionName, IServiceCallLifeCycle serviceCallLifeCycle, DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        instance = dataModel.getInstance(ServiceCallTypeImpl.class);
        instance.setName(name);
        instance.setVersionName(versionName);
        instance.setServiceCallLifeCycle(serviceCallLifeCycle);
        instance.setLogLevel(LogLevel.WARNING);
    }

    @Override
    public ServiceCallTypeBuilder logLevel(LogLevel logLevel) {
        Objects.requireNonNull(logLevel, "LogLevel must not be null");
        instance.setLogLevel(logLevel);
        return this;
    }

    @Override
    public ServiceCallTypeBuilder customPropertySet(RegisteredCustomPropertySet customPropertySet) {
        Objects.requireNonNull(customPropertySet);
        if (!customPropertySet.getCustomPropertySet().getDomainClass().isAssignableFrom(ServiceCall.class)) {
            throw new InvalidPropertySetDomainTypeException(thesaurus, MessageSeeds.INVALID_CPS_TYPE, customPropertySet);
        }
        this.toBeRegisteredCustomPropertySets.add(customPropertySet);
        return this;
    }

    @Override
    public ServiceCallTypeBuilder handler(String serviceCallHandler) {
        this.instance.setHandlerName(serviceCallHandler);
        return this;
    }

    @Override
    public ServiceCallType create() {
        instance.save();
        for (RegisteredCustomPropertySet customPropertySet : toBeRegisteredCustomPropertySets) {
            instance.addCustomPropertySet(customPropertySet);
        }

        return instance;
    }

}
