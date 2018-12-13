/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Optional;

/**
 * Created by dragos on 2/19/2016.
 */
public class BpmProcessPropertyImpl implements BpmProcessProperty {
    @NotNull(message = "{" + MessageSeeds.Constants.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String name;

    @NotNull(message = "{" + MessageSeeds.Constants.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String value;

    // Audit fields
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;

    @IsPresent
    private Reference<BpmProcessDefinition> processDefinition = ValueReference.absent();

    BpmProcessPropertyImpl init(BpmProcessDefinition processDefinition, String name, Object value) {
        this.processDefinition.set(processDefinition);
        this.name = name;
        setValue(value);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        Optional<PropertySpec> foundPropertySpec = getPropertySpec();
        if(foundPropertySpec.isPresent()) {
            return foundPropertySpec.get().getValueFactory().fromStringValue(value);
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        Optional<PropertySpec> foundPropertySpec = getPropertySpec();
        if(foundPropertySpec.isPresent()) {
            this.value = foundPropertySpec.get().getValueFactory().toStringValue(value);
        }
        else {
            this.value = value.toString();
        }
    }

    @Override
    public BpmProcessDefinition getProcessDefinition() {
        return processDefinition.get();
    }

    protected Optional<PropertySpec> getPropertySpec() {
        Optional<ProcessAssociationProvider> associationProvider = processDefinition.get().getAssociationProvider();
        if (associationProvider.isPresent()) {
            return associationProvider.get().getPropertySpec(getName());
        }
        return Optional.empty();
    }
}
