package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.UnknownProcessReferenceException;
import com.elster.jupiter.fsm.impl.constraints.Unique;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link State} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:20)
 */
@Unique(message = MessageSeeds.Keys.UNIQUE_STATE_NAME, groups = { Save.Create.class, Save.Update.class })
public class StateImpl implements State {

    public enum Fields {
        NAME("name"),
        OBSOLETE_TIMESTAMP("obsoleteTimestamp"),
        INITIAL("initial"),
        CUSTOM("custom"),
        FINITE_STATE_MACHINE("finiteStateMachine"),
        PROCESS_REFERENCES("processReferences");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final Clock clock;

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String name;
    private Instant obsoleteTimestamp;
    private boolean custom;
    private boolean initial;
    @IsPresent
    private Reference<FiniteStateMachine> finiteStateMachine = Reference.empty();
    @Valid
    private List<ProcessReferenceImpl> processReferences = new ArrayList<>();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    protected StateImpl(DataModel dataModel, Thesaurus thesaurus, Clock clock) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    public StateImpl initialize(FiniteStateMachine finiteStateMachine, boolean custom, String name) {
        this.setFiniteStateMachine(finiteStateMachine);
        this.setName(name);
        this.custom = custom;
        return this;
    }

    @Override
    public long getId() {
        return id;
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

    @Override
    public boolean isCustom() {
        return this.custom;
    }

    @Override
    public boolean isInitial() {
        return initial;
    }

    void setInitial(boolean initial) {
        this.initial = initial;
    }

    boolean isObsolete() {
        return this.obsoleteTimestamp != null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public FiniteStateMachine getFiniteStateMachine() {
        return finiteStateMachine.get();
    }

    void setFiniteStateMachine(FiniteStateMachine finiteStateMachine) {
        this.finiteStateMachine.set(finiteStateMachine);
    }

    @Override
    public List<StateTransition> getOutgoingStateTransitions() {
        return this.getFiniteStateMachine()
                .getTransitions()
                .stream()
                .filter(t -> t.getFrom().getId() == this.getId())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessReference> getOnEntryProcesses() {
        return this.getProcessReferences(ProcessReferenceImpl::isOnEntry);
    }

    @Override
    public List<ProcessReference> getOnExitProcesses() {
        return this.getProcessReferences(ProcessReferenceImpl::isOnExit);
    }

    private List<ProcessReference> getProcessReferences(Predicate<? super ProcessReferenceImpl> predicate) {
        return this.processReferences
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    void addOnEntry(String deploymentId, String processId) {
        this.processReferences.add(this.dataModel.getInstance(ProcessReferenceImpl.class).onEntry(this, deploymentId, processId));
    }

    void addOnExit(String deploymentId, String processId) {
        this.processReferences.add(this.dataModel.getInstance(ProcessReferenceImpl.class).onExit(this, deploymentId, processId));
    }

    void removeOnEntry(String deploymentId, String processId) {
        this.removeProcessReferences(deploymentId, processId, ProcessReferenceImpl::isOnEntry);
    }

    void removeOnExit(String deploymentId, String processId) {
        this.removeProcessReferences(deploymentId, processId, ProcessReferenceImpl::isOnExit);
    }

    private void removeProcessReferences(String deploymentId, String processId, Predicate<ProcessReferenceImpl> isEntryOrExit) {
        List<ProcessReferenceImpl> obsoleteReferences = this.processReferences
                .stream()
                .filter(isEntryOrExit)
                .filter(p -> p.matches(deploymentId, processId))
                .collect(Collectors.toList());
        if (obsoleteReferences.isEmpty()) {
            throw new UnknownProcessReferenceException(this.thesaurus, this, deploymentId, processId);
        }
        else {
            this.processReferences.removeAll(obsoleteReferences);
        }
    }

    void prepareDelete() {
        this.processReferences.clear();
    }

    void makeObsolete() {
        this.obsoleteTimestamp = this.clock.instant();
        this.prepareDelete();
        this.dataModel.update(this, Fields.OBSOLETE_TIMESTAMP.fieldName());
    }

    void save() {
        Save.UPDATE.save(this.dataModel, this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.getName() + ")";
    }

}