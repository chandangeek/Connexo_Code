package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ServiceCallImpl implements ServiceCall {
    private long id;
    private String name;
    private Instant creationDate;
    private Instant lastModificationDate;
    private Instant lastCompletedDate;
    private DefaultState state;
    private String origin;
    private String externalReference;
    private List<RegisteredCustomPropertySet> customProperties;
    private RefAny targetObject;
    private ServiceCall parent;
    private Reference<ServiceCallType> type;

    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;


    private final DataModel dataModel;

    @Inject
    public ServiceCallImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public enum Fields {
        name("name"),
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
    public Instant getCreationDate() {
        return this.creationDate;
    }

    @Override
    public Instant getLastModificationDate() {
        return this.lastModificationDate;
    }

    @Override
    public Optional<Instant> getLastCompletedDate() {
        return  Optional.ofNullable(this.lastCompletedDate);
    }

    @Override
    public DefaultState getState() {
        return this.state;
    }

    @Override
    public void setState(DefaultState state) {

    }

    @Override
    public Optional<String> getOrigin() {
        return  Optional.ofNullable(this.origin);
    }

    @Override
    public Optional<String> getExternalReference() {
        return  Optional.ofNullable(this.externalReference);
    }

    @Override
    public Optional<List<RegisteredCustomPropertySet>> getCustomProperties() {
        return Optional.ofNullable(this.customProperties);
    }

    @Override
    public Optional<RefAny> getTargetObject() {
        return  Optional.ofNullable(this.targetObject);
    }

    @Override
    public Optional<ServiceCall> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public void cancel() {

    }

    @Override
    public ServiceCallType getType() {
        return this.type.orNull();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
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
