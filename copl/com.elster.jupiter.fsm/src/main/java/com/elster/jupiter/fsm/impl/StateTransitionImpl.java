package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.util.Optional;
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
        NAME("name"),
        FINITE_STATE_MACHINE("finiteStateMachine"),
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

    StateTransitionImpl initialize(FiniteStateMachine stateMachine, State from, State to, StateTransitionEventType eventType) {
        this.finiteStateMachine.set(stateMachine);
        this.from.set(from);
        this.to.set(to);
        this.eventType.set(eventType);
        return this;
    }

    private long id;
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String name;
    @IsPresent
    private Reference<FiniteStateMachine> finiteStateMachine = Reference.empty();
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
    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName(Thesaurus thesaurus) {
        return this.getName()
                .orElseGet(() -> this.getNameUsingThesaurus(thesaurus));
    }

    private String getNameUsingThesaurus(Thesaurus thesaurus) {
        String symbol = this.getEventType().getSymbol();
        return thesaurus.getString(symbol, symbol);
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

    void prepareDelete() {
        this.from.set(null);
        this.to.set(null);
    }

    // Mostly here for debugging purposes
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