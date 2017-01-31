/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallLog;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Created by bvn on 3/1/16.
 */
public class ServiceCallLogImpl implements ServiceCallLog, HasId {

    enum Fields {
        timestamp("timestamp"),
        logLevel("logLevel"),
        serviceCall("serviceCall"),
        message("message"),
        stackTrace("stackTrace");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;

    @Inject
    public ServiceCallLogImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private long id;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED_FIELD + "}")
    private Instant timestamp;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED_FIELD + "}")
    private LogLevel logLevel;
    private Reference<ServiceCall> serviceCall = Reference.empty();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED_FIELD + "}")
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED_FIELD + "}")
    private String message;
    private String stackTrace;

    static ServiceCallLogImpl from(DataModel dataModel, ServiceCallImpl serviceCall, String message, LogLevel level, Instant timestamp, String stackTrace) {
        return dataModel.getInstance(ServiceCallLogImpl.class)
                .init(serviceCall, message, level, timestamp, stackTrace);
    }

    public ServiceCallLogImpl init(ServiceCall serviceCall, String message, LogLevel logLevel, Instant timestamp, String stackTrace) {
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.serviceCall.set(serviceCall);
        this.message = message;
        this.stackTrace = stackTrace;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public Instant getTime() {
        return timestamp;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getStackTrace() {
        return stackTrace;
    }

    public void save() {
        if (this.getId() > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }

    @Override
    public void delete() {
        this.dataModel.remove(this);
    }
}
