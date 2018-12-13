/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.CannotDeleteServiceCallType;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.HandlerDisappearedException;
import com.elster.jupiter.servicecall.InvalidPropertySetDomainTypeException;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.Status;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Class models the type of a service call. The type defines the life cycle its service calls will abide by and links to
 * the custom properties that are available for use.
 */

public class ServiceCallTypeImpl implements IServiceCallType {
    private long id;
    private String name;
    private String versionName;
    private Status status;
    private LogLevel logLevel;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED_FIELD + "}")
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED_FIELD + "}")
//    @IsRegisteredHandler has been removed, as handlers who are not yet registered on the white board should not cause validation errors
    private String serviceCallHandler;
    private Reference<IServiceCallLifeCycle> serviceCallLifeCycle = Reference.empty();
    private DefaultState currentLifeCycleState;
    private List<ServiceCallTypeCustomPropertySetUsage> customPropertySets = new ArrayList<>();
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;


    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final IServiceCallService serviceCallService;

    @Inject
    public ServiceCallTypeImpl(DataModel dataModel, IServiceCallService serviceCallService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.status = Status.ACTIVE;
    }

    public enum Fields {
        name("name"),
        serviceCallLifeCycle("serviceCallLifeCycle"),
        logLevel("logLevel"),
        status("status"),
        versionName("versionName"),
        version("version"),
        currentLifeCycleState("currentLifeCycleState"),
        customPropertySets("customPropertySets"),
        handler("serviceCallHandler");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVersionName() {
        return versionName;
    }

    void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    @Override
    public ServiceCallHandler getServiceCallHandler() {
        return new LoggingServiceCallHandler(doGetServiceCallHandler(), thesaurus);
    }

    private ServiceCallHandler doGetServiceCallHandler() {
        return serviceCallService.findHandler(serviceCallHandler)
                .orElseThrow(() -> new HandlerDisappearedException(thesaurus, MessageSeeds.HANDLER_DISAPPEARED, serviceCallHandler));
    }

    public String getHandlerName() {
        return serviceCallHandler;
    }

    public void setHandlerName(String serviceCallHandler) {
        this.serviceCallHandler = serviceCallHandler;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void deprecate() {
        this.status = Status.DEPRECATED;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public IServiceCallLifeCycle getServiceCallLifeCycle() {
        return serviceCallLifeCycle.get();
    }

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySets() {
        return customPropertySets.stream().map(ServiceCallTypeCustomPropertySetUsage::getCustomPropertySet).collect(toList());
    }

    void setServiceCallLifeCycle(IServiceCallLifeCycle serviceCallLifeCycle) {
        this.serviceCallLifeCycle.set(serviceCallLifeCycle);
    }

    @Override
    public void addCustomPropertySet(RegisteredCustomPropertySet customPropertySet) {
        Objects.requireNonNull(customPropertySet);
        if (!customPropertySet.getCustomPropertySet().getDomainClass().isAssignableFrom(ServiceCall.class)) {
            throw new InvalidPropertySetDomainTypeException(thesaurus, MessageSeeds.INVALID_CPS_TYPE, customPropertySet);
        }
        ServiceCallTypeCustomPropertySetUsageImpl usage = dataModel.getInstance(ServiceCallTypeCustomPropertySetUsageImpl.class);
        usage.initialize(this, customPropertySet);
        this.customPropertySets.add(usage);
    }

    @Override
    public void removeCustomPropertySet(RegisteredCustomPropertySet customPropertySet) {
        Objects.requireNonNull(customPropertySet);
        customPropertySets.stream()
                .filter(usage -> usage.getServiceCallType().getId() == this.getId() && usage.getCustomPropertySet().getId() == customPropertySet.getId())
                .findFirst()
                .ifPresent(this.customPropertySets::remove);
    }

    @Override
    public void save() {
        if (this.getId() > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }

    }

    @Override
    public ServiceCallBuilder newServiceCall() {
        return ServiceCallBuilderImpl.from(dataModel, this);
    }

    @Override
    public void delete() {
        dataModel.stream(ServiceCall.class)
                .filter(Where.where(ServiceCallImpl.Fields.type.fieldName()).isEqualTo(this))
                .limit(1)
                .findAny()
                .ifPresent(oneOfThisType -> {
                    throw new CannotDeleteServiceCallType(thesaurus, MessageSeeds.CANNOT_DELETE_SERVICECALLTYPE, this, oneOfThisType);
                });

        dataModel.mapper(IServiceCallType.class).remove(this);
    }

}
