/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.EndPointConfigurationReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.impl.constraints.IsActive;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.Checks;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;

/**
 * Provides an implementation for the {@link EndPointConfigurationReference} interface.
 */
public class EndPointConfigurationReferenceImpl implements EndPointConfigurationReference {

    public enum Fields {
        STATE("state"),
        ENDPOINT_CONFIGURATION("endPointConfiguration"),
        PURPOSE("purpose");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @IsPresent
    @IsActive
    private Reference<EndPointConfiguration> endPointConfiguration = Reference.empty();
    @IsPresent
    private Reference<State> state = Reference.empty();
    @NotNull
    private Purpose purpose;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    EndPointConfigurationReferenceImpl onEntry(State state, EndPointConfiguration endPointConfiguration) {
        return this.initialize(state, Purpose.OnEntry, endPointConfiguration);
    }

    EndPointConfigurationReferenceImpl onExit(State state, EndPointConfiguration endPointConfiguration) {
        return this.initialize(state, Purpose.OnExit, endPointConfiguration);
    }

    private EndPointConfigurationReferenceImpl initialize(State state, Purpose purpose, EndPointConfiguration endPointConfiguration) {
        this.state.set(state);
        this.purpose = purpose;
        this.endPointConfiguration.set(endPointConfiguration);
        return this;
    }

    @Override
    public EndPointConfiguration getStateChangeEndPointConfiguration() {
        return endPointConfiguration.get();
    }

    public boolean isOnEntry() {
        return Purpose.OnEntry.equals(this.purpose);
    }

    public boolean isOnExit() {
        return Purpose.OnExit.equals(this.purpose);
    }

    public boolean matches(EndPointConfiguration endPointConfiguration) {
        return Checks.is(this.endPointConfiguration.get().getId()).equalTo(endPointConfiguration.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndPointConfigurationReferenceImpl that = (EndPointConfigurationReferenceImpl) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}