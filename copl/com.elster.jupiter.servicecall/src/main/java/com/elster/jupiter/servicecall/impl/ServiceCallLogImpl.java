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
        message("message");

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

    public void init(Instant timestamp, LogLevel logLevel, ServiceCall serviceCall, String message) {
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.serviceCall.set(serviceCall);
        this.message = message;
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

    public void save() {
        if (this.getId() > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }

}
