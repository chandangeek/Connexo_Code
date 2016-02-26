package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.InvalidPropertySetDomainTypeException;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.Status;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

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

    @Inject
    public ServiceCallTypeImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
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
        customPropertySets("customPropertySets");

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
    public Optional<ServiceCallLifeCycle> getServiceCallLifeCycle() {
        return serviceCallLifeCycle.getOptional();
    }

    @Override
    public Optional<DefaultState> getCurrentLifeCycleState() {
        return Optional.ofNullable(currentLifeCycleState);
    }

    @Override
    public void setCurrentLifeCycleState(DefaultState currentLifeCycleState) {
        // todo transition life cycle
        this.currentLifeCycleState = currentLifeCycleState;
    }

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySets() {
        return customPropertySets.stream().map(ServiceCallTypeCustomPropertySetUsage::getCustomPropertySet).collect(toList());
    }

    void setServiceCallLifeCycle(ServiceCallLifeCycle serviceCallLifeCycle) {
        this.serviceCallLifeCycle.set(serviceCallLifeCycle);
    }

    @Override
    public void addCustomPropertySet(RegisteredCustomPropertySet customPropertySet) {
        Objects.requireNonNull(customPropertySet);
        if (!customPropertySet.getCustomPropertySet().getDomainClass().isAssignableFrom(ServiceCallType.class)) {
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
}
