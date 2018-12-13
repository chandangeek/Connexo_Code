/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionEventTypeStillInUseException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Provides an implementation for the {@link StateTransitionEventType} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:03)
 */
public abstract class StateTransitionEventTypeImpl implements StateTransitionEventType {

    public enum Fields {
        SYMBOL("symbol"),
        CONTEXT("context"),
        EVENT_TYPE("eventType");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    public static final String CUSTOM = "0";
    public static final String STANDARD = "1";
    public static final Map<String, Class<? extends StateTransitionEventType>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends StateTransitionEventType>>of(
                    CUSTOM, CustomStateTransitionEventTypeImpl.class,
                    STANDARD, StandardStateTransitionEventTypeImpl.class);

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final ServerFiniteStateMachineService stateMachineService;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String context;

    @SuppressWarnings("unused")
    private long id;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    protected StateTransitionEventTypeImpl(DataModel dataModel, Thesaurus thesaurus, ServerFiniteStateMachineService stateMachineService) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.stateMachineService = stateMachineService;
    }

    protected void initialize(String context) {
        this.context = context;
    }

    @Override
    public long getId() {
        return id;
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreationTimestamp() {
        return createTime;
    }

    @Override
    public Instant getModifiedTimestamp() {
        return modTime;
    }

    void save() {
        Save.CREATE.save(this.dataModel, this);
    }

    @Override
    public void update() {
        Save.UPDATE.save(this.dataModel, this);
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public void delete() {
        this.validateDelete();
        this.dataModel.remove(this);
    }

    private void validateDelete() {
        List<FiniteStateMachine> stateMachines = this.stateMachineService.findFiniteStateMachinesUsing(this);
        if (!stateMachines.isEmpty()) {
            throw new StateTransitionEventTypeStillInUseException(this.thesaurus, this, stateMachines);
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

        StateTransitionEventTypeImpl that = (StateTransitionEventTypeImpl) o;

        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}