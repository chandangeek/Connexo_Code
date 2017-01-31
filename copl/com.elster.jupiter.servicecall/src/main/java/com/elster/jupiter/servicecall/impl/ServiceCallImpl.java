/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallLog;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.fill;

public class ServiceCallImpl implements ServiceCall {
    static final int ZEROFILL_SIZE = 8;
    private static final NumberFormat NUMBER_PREFIX = createFormat();
    private final Thesaurus thesaurus;

    private static NumberFormat createFormat() {
        char[] zeroFillChars = new char[ZEROFILL_SIZE];
        fill(zeroFillChars, '0');
        return new DecimalFormat("SC_" + new String(zeroFillChars));
    }
    private final IServiceCallService serviceCallService;

    private long id;
    private Instant lastCompletedTime;
    private String origin;
    private String externalReference;
    private RefAny targetObject;
    private Reference<ServiceCall> parent = ValueReference.absent();
    private Reference<IServiceCallType> type = ValueReference.absent();
    private Reference<State> state = ValueReference.absent();

    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;


    private final DataModel dataModel;
    private final Clock clock;
    private final CustomPropertySetService customPropertySetService;

    enum Fields {
        lastCompletedTime("lastCompletedTime"),
        origin("origin"),
        externalReference("externalReference"),
        targetObject("targetObject"),
        parent("parent"),
        type("type"),
        state("state"),
        createTime("createTime"),
        modTime("modTime"),
        userName("userName"),
        version("version");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    ServiceCallImpl(DataModel dataModel, IServiceCallService serviceCallService, Clock clock, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.customPropertySetService = customPropertySetService;
        this.thesaurus = thesaurus;
    }

    static ServiceCallImpl from(DataModel dataModel, IServiceCallType type) {
        return dataModel.getInstance(ServiceCallImpl.class).init(null, type);
    }

    static ServiceCallImpl from(DataModel dataModel, ServiceCall parent, IServiceCallType type) {
        return dataModel.getInstance(ServiceCallImpl.class).init(parent, type);
    }

    private ServiceCallImpl init(ServiceCall parent, IServiceCallType type) {
        this.parent.set(parent);
        this.type.set(type);
        this.state.set(asState(DefaultState.CREATED));
        return this;
    }

    @Override
    public String getNumber() {
        return NUMBER_PREFIX.format(getId());
    }

    @Override
    public Instant getCreationTime() {
        return this.createTime;
    }

    @Override
    public Instant getLastModificationTime() {
        return this.modTime;
    }

    @Override
    public Optional<Instant> getLastCompletedTime() {
        return Optional.ofNullable(this.lastCompletedTime);
    }

    @Override
    public DefaultState getState() {
        return Arrays.stream(DefaultState.values())
                .filter(defaultState -> defaultState.getKey().equals(state.get().getName()))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public void requestTransition(DefaultState defaultState) {
        getType().getServiceCallLifeCycle().triggerTransition(this, defaultState);
    }

    void setState(DefaultState defaultState) {
        this.state.set(asState(defaultState));
    }

    private State asState(DefaultState state) {
        return getType().getServiceCallLifeCycle().getState(state).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public Optional<String> getOrigin() {
        return Optional.ofNullable(this.origin);
    }

    void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public Optional<String> getExternalReference() {
        return Optional.ofNullable(this.externalReference);
    }

    void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    @Override
    public Optional<?> getTargetObject() {
        return this.targetObject.getOptional();
    }

    void setTargetObject(Object targetObject) {
        this.targetObject = dataModel.asRefAny(targetObject);
    }

    @Override
    public Optional<ServiceCall> getParent() {
        return parent.getOptional();
    }

    @Override
    public void cancel() {
        requestTransition(DefaultState.CANCELLED);
    }

    @Override
    public IServiceCallType getType() {
        return this.type.orNull();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public ServiceCallBuilder newChildCall(ServiceCallType serviceCallType) {
        return ServiceCallBuilderImpl.from(dataModel, this, (IServiceCallType) serviceCallType);
    }

    @Override
    public Finder<ServiceCallLog> getLogs() {
        return DefaultFinder.of(ServiceCallLog.class,
                Where.where(ServiceCallLogImpl.Fields.serviceCall.fieldName())
                        .isEqualTo(this), dataModel).sorted(ServiceCallLogImpl.Fields.timestamp.fieldName(), false);
    }

    @Override
    public void log(LogLevel logLevel, String message) {
        if (type.get().getLogLevel().compareTo(logLevel) > -1) {
            ServiceCallLogImpl instance = ServiceCallLogImpl.from(dataModel, this, message, logLevel, clock.instant(), null);
            instance.save();
        }
    }

    @Override
    public void log(String message, Exception exception) {
        LogLevel level = LogLevel.SEVERE;
        if (type.get().getLogLevel().compareTo(level) > -1) {
            ServiceCallLogImpl instance = ServiceCallLogImpl.from(dataModel, this, message, level, clock.instant(), stackTrace2String(exception));
            instance.save();
        }
    }

    private String stackTrace2String(Exception e) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (PrintWriter printWriter = new PrintWriter(byteArrayOutputStream)) {
            e.printStackTrace(printWriter);
        }
        return byteArrayOutputStream.toString();
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceCallImpl that = (ServiceCallImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public Finder<ServiceCall> findChildren(ServiceCallFilter filter) {
        filter.parent = this;
        return serviceCallService.getServiceCallFinder(filter);
    }

    @Override
    public Finder<ServiceCall> findChildren() {
        return DefaultFinder.of(ServiceCall.class, Where.where(Fields.parent.fieldName()).isEqualTo(this), dataModel)
                .maxPageSize(thesaurus, 1000);
    }

    @Override
    public <T extends PersistentDomainExtension<ServiceCall>> Optional<T> getExtensionFor(CustomPropertySet<ServiceCall, T> customPropertySet, Object... additionalPrimaryKeyValues) {
        return customPropertySetService.getUniqueValuesEntityFor(customPropertySet, this, additionalPrimaryKeyValues);
    }

    @Override
    public <T extends PersistentDomainExtension<ServiceCall>> CustomPropertySetValues getValuesFor(CustomPropertySet<ServiceCall, T> customPropertySet, Object... additionalPrimaryKeyValues) {
        return customPropertySetService.getUniqueValuesFor(customPropertySet, this, additionalPrimaryKeyValues);
    }

    @Override
    public <T extends PersistentDomainExtension<ServiceCall>> Optional<T> getExtension(Class<T> extensionClass, Object... additionalPrimaryKeyValues) {
        return customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                .stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .filter(customPropertySet -> customPropertySet.getPersistenceSupport()
                        .persistenceClass()
                        .equals(extensionClass))
                .map(customPropertySet -> (CustomPropertySet<ServiceCall, T>) customPropertySet)
                .findAny()
                .flatMap(customPropertySet -> getExtensionFor(customPropertySet, additionalPrimaryKeyValues));
    }

    @Override
    public void update(PersistentDomainExtension<ServiceCall> extension, Object... additionalPrimaryKeyValues) {
        CustomPropertySet customPropertySet = getType().getCustomPropertySets()
                .stream()
                .map(registeredCustomPropertySet -> registeredCustomPropertySet.getCustomPropertySet())
                .filter(customSet -> customSet.getPersistenceSupport()
                        .persistenceClass()
                        .isAssignableFrom(extension.getClass()))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
        customPropertySetService.setValuesFor(customPropertySet, this, extension, additionalPrimaryKeyValues);
    }

    @Override
    public void delete() {
        deleteQueuedTransitions();
        deleteCustomPropertySetsRecursive();
        this.dataModel.remove(this);
    }

    void deleteCustomPropertySetsRecursive() {
        for (RegisteredCustomPropertySet registeredCustomPropertySet : customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)) {
            customPropertySetService.removeValuesFor(registeredCustomPropertySet.getCustomPropertySet(), this);
        }
        this.findChildren().stream().forEach(sc -> ((ServiceCallImpl) sc).deleteCustomPropertySetsRecursive());
    }

    private void deleteQueuedTransitions() {
        DestinationSpec serviceCallQueue = serviceCallService.getServiceCallQueue();
        serviceCallQueue.purgeCorrelationId(this.getNumber());
    }

    @Override
    public boolean canTransitionTo(DefaultState targetState) {
        return getType().getServiceCallLifeCycle().canTransition(getState(), targetState);
    }
}
