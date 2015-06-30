package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.Checks;

import javax.validation.constraints.NotNull;

/**
 * Provides an implementation for the {@link ProcessReference} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (13:02)
 */
public class ProcessReferenceImpl implements ProcessReference {

    public enum Fields {
        STATE("state"),
        PROCESS("process"),
        PURPOSE("purpose");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused")
    private long id;
    @IsPresent
    private Reference<StateChangeBusinessProcess> process = Reference.empty();
    @IsPresent
    private Reference<State> state = Reference.empty();
    @NotNull
    private Purpose purpose;

    private enum Purpose {
        OnEntry, OnExit;
    }

    ProcessReferenceImpl onEntry(State state, StateChangeBusinessProcess process) {
        return this.initialize(state, Purpose.OnEntry, process);
    }

    ProcessReferenceImpl onExit(State state, StateChangeBusinessProcess process) {
        return this.initialize(state, Purpose.OnExit, process);
    }

    private ProcessReferenceImpl initialize(State state, Purpose purpose, StateChangeBusinessProcess process) {
        this.state.set(state);
        this.purpose = purpose;
        this.process.set(process);
        return this;
    }

    @Override
    public StateChangeBusinessProcess getStateChangeBusinessProcess() {
        return process.get();
    }

    public boolean isOnEntry() {
        return Purpose.OnEntry.equals(this.purpose);
    }

    public boolean isOnExit() {
        return Purpose.OnExit.equals(this.purpose);
    }

    public boolean matches(StateChangeBusinessProcess process) {
        return Checks.is(this.process.get().getId()).equalTo(process.getId());
    }

}