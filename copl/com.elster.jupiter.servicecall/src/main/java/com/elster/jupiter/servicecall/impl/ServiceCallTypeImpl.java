package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.Status;

import java.time.Instant;
import java.util.Optional;

/**
 * Class models the type of a service call. The type defines the life cycle its service calls will abide by and links to
 * the custom properties that are available for use.
 */
public class ServiceCallTypeImpl implements ServiceCallType {
    private long id;
    private String name;
    private String versionName;
    private Status status;
    private LogLevel logLevel;
    private Reference<ServiceCallLifeCycle> serviceCallLifeCycle = Reference.empty();
    private DefaultState currentLifeCycleState;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;

    public enum Fields {
        name("name"),
        serviceCallLifeCycle("serviceCallLifeCycle"),
        logLevel("logLevel"),
        status("status"),
        versionName("versionName"),
        version("version"),
        currentLifeCycleState("currentLifeCycleState");

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

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVersionName() {
        return versionName;
    }

    @Override
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
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
    public Optional<ServiceCallLifeCycle> getServiceCallLifeCycle() {
        return serviceCallLifeCycle.getOptional();
    }

    public Optional<DefaultState> getCurrentLifeCycleState() {
        return Optional.ofNullable(currentLifeCycleState);
    }

    public void setCurrentLifeCycleState(DefaultState currentLifeCycleState) {
        // todo transition life cycle
        this.currentLifeCycleState = currentLifeCycleState;
    }

    @Override
    public Optional<CustomPropertySet<ServiceCall, ? extends PersistentDomainExtension<ServiceCall>>> getCustomPropertySet() {
        return Optional.empty();
    }



    @Override
    public void save() {
        // TODO
    }
}
