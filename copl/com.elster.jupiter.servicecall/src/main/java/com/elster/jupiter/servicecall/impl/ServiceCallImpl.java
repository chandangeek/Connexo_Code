package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class ServiceCallImpl implements ServiceCall {
    public static final NumberFormat NUMBER_PREFIX = new DecimalFormat("SC_00000000");
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
    ServiceCallImpl(DataModel dataModel, Clock clock, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.customPropertySetService = customPropertySetService;
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
    public void setState(DefaultState defaultState) {
        // TODO invoke FSM checks, for now a plain setter
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
    public Finder<ServiceCall> getChildren() {
        Condition condition = Where.where("parent").isEqualTo(this);
        return DefaultFinder.of(ServiceCall.class, condition, dataModel).defaultSortColumn(ServiceCallImpl.Fields.type.fieldName());
    }

    @Override
    public <T extends PersistentDomainExtension<ServiceCall>> Optional<T> getExtensionFor(CustomPropertySet<ServiceCall, T> customPropertySet, Object... additionalPrimaryKeyValues) {
        return customPropertySetService.getUniqueValuesEntityFor(customPropertySet, this, additionalPrimaryKeyValues);
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


}
