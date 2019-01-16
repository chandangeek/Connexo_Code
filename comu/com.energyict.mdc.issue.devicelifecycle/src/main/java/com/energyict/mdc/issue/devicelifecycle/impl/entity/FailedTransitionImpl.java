/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.entity;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.issue.devicelifecycle.FailedTransition;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class FailedTransitionImpl implements FailedTransition {

    public enum Fields {
        ISSUE("issue"),
        LIFECYCLE("lifecycle"),
        TRANSITION("transition"),
        FROM("from"),
        TO("to"),
        CAUSE("cause"),
        MODTIME("modTime");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<DeviceLifeCycle> lifecycle = ValueReference.absent();

    @IsPresent
    private Reference<StateTransition> transition = ValueReference.absent();

    @IsPresent
    private Reference<State> from = Reference.empty();
    @IsPresent
    private Reference<State> to = Reference.empty();

    private String cause;

    @NotNull
    private Instant modTime;


    FailedTransitionImpl init(DeviceLifeCycle deviceLifeCycle, StateTransition stateTransition,
                              State from, State to, Instant modTime, String cause) {
        this.lifecycle.set(deviceLifeCycle);
        this.transition.set(stateTransition);
        this.from.set(from);
        this.to.set(to);
        this.modTime = modTime;
        this.cause = cause;
        return this;

    }


    @Override
    public DeviceLifeCycle getLifecycle() {
        return lifecycle.get();
    }

    @Override
    public StateTransition getTransition() {
        return transition.get();
    }

    @Override
    public State getFrom() {
        return from.get();
    }

    @Override
    public State getTo() {
        return to.get();
    }

    @Override
    public String getCause() {
        return cause;
    }

    @Override
    public Instant getOccurrenceTime() {
        return modTime;
    }
}
