package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link StateTransitionImpl} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:29)
 */
public class StateTransitionImpl implements StateTransition {

    public enum Fields {
        FINATE_STATE_MACHINE("finateStateMachine"),
        FROM("from"),
        TO("to"),
        EVENT_TYPE("eventType");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    StateTransitionImpl initialize(FinateStateMachine stateMachine, State from, State to, StateTransitionEventType eventType) {
        this.finateStateMachine.set(stateMachine);
        this.from.set(from);
        this.to.set(to);
        this.eventType.set(eventType);
        return this;
    }

    private long id;
    @IsPresent
    private Reference<FinateStateMachine> finateStateMachine = Reference.empty();
    @IsPresent
    private Reference<State> from = Reference.empty();
    @IsPresent
    private Reference<State> to = Reference.empty();
    @IsPresent
    private Reference<StateTransitionEventType> eventType = Reference.empty();

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public State getFrom() {
        return this.from.get();
    }

    @Override
    public State getTo() {
        return this.to.get();
    }

    @Override
    public StateTransitionEventType getEventType() {
        return this.eventType.get();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.toStringAttributes().collect(Collectors.joining(", ")) +")";
    }

    private Stream<String> toStringAttributes() {
        return Stream.of(
             "from:" + this.getFrom().getName(),
             "to:" + this.getTo().getName(),
             "eventTYpe:" + this.getEventType().getSymbol()
        );
    }

}