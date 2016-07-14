package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.impl.constraints.Unique;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link StateTransitionImpl} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:29)
 */
@Unique(message = "{" + MessageSeeds.Keys.DUPLICATE_STATE_TRANSITION + "}", groups = { Save.Create.class, Save.Update.class })
public final class StateTransitionImpl implements StateTransition {

    public enum Fields {
        NAME("name"),
        NAME_KEY("nameTranslationKey"),
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

    @SuppressWarnings("unused")
    private long id;
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String nameTranslationKey;
    @IsPresent
    private Reference<FiniteStateMachine> finiteStateMachine = Reference.empty();
    @IsPresent
    private Reference<State> from = Reference.empty();
    @IsPresent
    private Reference<State> to = Reference.empty();
    @IsPresent
    private Reference<StateTransitionEventType> eventType = Reference.empty();
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Override
    public long getId() {
        return this.id;
    }

    public FiniteStateMachine getFiniteStateMachine() {
        return finiteStateMachine.get();
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public Optional<String> getTranslationKey() {
        return Optional.ofNullable(this.nameTranslationKey);
    }

    void setTranslationKey(String nameTranslationKey) {
        this.nameTranslationKey = nameTranslationKey;
    }

    @Override
    public String getName(Thesaurus thesaurus) {
        if (this.getName().isPresent()){
            return this.getName().get();
        }
        else if (this.getTranslationKey().isPresent()) {
            return this.getNameUsingThesaurus(thesaurus, this.getTranslationKey().get());
        }
        else {
            return this.getNameUsingThesaurus(thesaurus, this.getEventType().getSymbol());
        }
    }

    private String getNameUsingThesaurus(Thesaurus thesaurus, String key) {
        return thesaurus.getString(key, key);
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

    /**
     * Tests if this StateTransitionImpl is a duplicate for the
     * other StateTransitionImpl.
     * It is a duplicate if the source and eventType are the same.
     *
     * @param otherTransition The other StateTransitionImpl
     * @return A flag that indicates if this StateTransitionImpl duplicates the other
     */
    public boolean duplicates(StateTransitionImpl otherTransition) {
        return !this.theSame(otherTransition)
            && this.sameSource(otherTransition)
            && this.sameEventType(otherTransition);
    }

    private boolean theSame(StateTransitionImpl otherTransition) {
        return this == otherTransition;
    }

    private boolean sameSource(StateTransitionImpl otherTransition) {
        return fromStateOf(this).isEqualTo(otherTransition.from);
    }

    private boolean sameEventType(StateTransitionImpl otherTransition) {
        return reference(this.eventType).isEqualTo(otherTransition.eventType);
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
             "eventType:" + this.getEventType().getSymbol()
        );
    }

    private ReferenceStateChecker fromStateOf(StateTransitionImpl transition) {
        return new ReferenceStateChecker(transition.from);
    }

    private StateChecker is(State first) {
        long id = first.getId();
        if (id > 0) {
            return new PersistentStateChecker(first);
        }
        else {
            return new MemoryStateChecker(first);
        }
    }

    private ReferenceEventTypeChecker reference(Reference<StateTransitionEventType> first) {
        return new ReferenceEventTypeChecker(first);
    }

    private EventTypeChecker is(StateTransitionEventType first) {
        return new PersistentEventTypeChecker(first);
    }

    private final class ReferenceStateChecker {

        private final Reference<State> target;

        private ReferenceStateChecker(Reference<State> target) {
            super();
            this.target = target;
        }

        private boolean isEqualTo(Reference<State> other) {
            return (this.target.isPresent() && other.isPresent())
                    && is(this.target.get()).equalTo(other.get());
        }

    }

    private interface StateChecker {
        public boolean equalTo(State other);
    }

    private final class MemoryStateChecker implements StateChecker {
        private final State target;

        private MemoryStateChecker(State target) {
            super();
            this.target = target;
        }

        @Override
        public boolean equalTo(State other) {
            return this.target == other;
        }

    }

    private final class PersistentStateChecker implements StateChecker {
        private final State target;

        private PersistentStateChecker(State target) {
            super();
            this.target = target;
        }

        @Override
        public boolean equalTo(State other) {
            return this.target.getId() == other.getId();
        }

    }

    private final class ReferenceEventTypeChecker {

        private final Reference<StateTransitionEventType> target;

        private ReferenceEventTypeChecker(Reference<StateTransitionEventType> target) {
            super();
            this.target = target;
        }

        private boolean isEqualTo(Reference<StateTransitionEventType> other) {
            return (this.target.isPresent() && other.isPresent())
                && is(this.target.get()).equalTo(other.get());
        }

    }

    private interface EventTypeChecker {
        public boolean equalTo(StateTransitionEventType other);
    }

    private final class PersistentEventTypeChecker implements EventTypeChecker {
        private final StateTransitionEventType target;

        private PersistentEventTypeChecker(StateTransitionEventType target) {
            super();
            this.target = target;
        }

        @Override
        public boolean equalTo(StateTransitionEventType other) {
            return this.target.getId() == other.getId();
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

        StateTransitionImpl that = (StateTransitionImpl) o;

        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}