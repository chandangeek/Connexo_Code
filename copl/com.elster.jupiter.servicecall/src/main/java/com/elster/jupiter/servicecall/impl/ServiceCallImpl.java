package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;
import java.sql.Ref;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ServiceCallImpl implements ServiceCall {
    public static final NumberFormat NUMBER_PREFIX = new DecimalFormat("SC_00000000");
    private long id;
    private Instant lastCompletedDate;
    private String origin;
    private String externalReference;
    private RefAny targetObject;
    private ServiceCall parent;
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

    @Inject
    ServiceCallImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    static ServiceCallImpl from(DataModel dataModel, IServiceCallType type) {
        return dataModel.getInstance(ServiceCallImpl.class).init(type);
    }

    private ServiceCallImpl init(IServiceCallType type) {
        this.type.set(type);
        this.state.set(asState(DefaultState.CREATED));
        return this;
    }

    public enum Fields {
        creationDate("creationDate"),
        lastModificationDate("lastModificationDate"),
        lastCompletedDate("lastCompletedDate"),
        state("state"),
        origin("origin"),
        externalReference("externalReference"),
        customProperties("customProperties"),
        targetObject("targetObject"),
        parent("parent"),
        type("type");


        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
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
        return  Optional.ofNullable(this.lastCompletedDate);
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
        return  Optional.ofNullable(this.origin);
    }

    void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public Optional<String> getExternalReference() {
        return  Optional.ofNullable(this.externalReference);
    }

    void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    @Override
    public Optional<List<RegisteredCustomPropertySet>> getCustomProperties() {
        //return customPropertySetService.get;
        return null;
    }

    @Override
    public Optional<RefAny> getTargetObject() {
        return  Optional.ofNullable(this.targetObject);
    }

    void setTargetObject(RefAny targetObject) {
        this.targetObject = targetObject;
    }

    @Override
    public Optional<ServiceCall> getParent() {
        return Optional.ofNullable(this.parent);
    }

    void setParent(ServiceCall parent) {
        this.parent = parent;
    }

    @Override
    public void cancel() {

    }

    @Override
    public IServiceCallType getType() {
        return this.type.orNull();
    }

    @Override
    public long getId() {
        return this.id;
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
