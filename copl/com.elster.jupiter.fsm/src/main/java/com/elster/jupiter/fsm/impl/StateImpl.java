/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.domain.util.AllowedChars;
import com.elster.jupiter.domain.util.HasOnlyWhiteListedCharacters;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.EndPointConfigurationReference;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.UnknownProcessReferenceException;
import com.elster.jupiter.fsm.impl.constraints.Unique;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link State} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:20)
 */
@Unique(message = MessageSeeds.Keys.UNIQUE_STATE_NAME, groups = {Save.Create.class, Save.Update.class})
public final class StateImpl implements State {

    public enum Fields {
        NAME("name"),
        OBSOLETE_TIMESTAMP("obsoleteTimestamp"),
        INITIAL("initial"),
        CUSTOM("custom"),
        FINITE_STATE_MACHINE("finiteStateMachine"),
        PROCESS_REFERENCES("processReferences"),
        ENDPOINT_CONFIGURATION_REFERENCES("endPointConfigurationReferences"),
        STAGE("stage");

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
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.TEXT_FEILD_CHARS)
    private String name;
    private Instant obsoleteTimestamp;
    private boolean custom;
    private boolean initial;
    @IsPresent
    private Reference<FiniteStateMachine> finiteStateMachine = Reference.empty();
    @Valid
    private List<ProcessReferenceImpl> processReferences = new ArrayList<>();
    @Valid
    private List<EndPointConfigurationReferenceImpl> endPointConfigurationReferences = new ArrayList<>();
    private Reference<Stage> stage = Reference.empty();
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

    protected void setStage(Stage stage) {
        this.stage.set(stage);
    }

    public StateImpl initialize(FiniteStateMachine finiteStateMachine, boolean custom, String name) {
        this.setFiniteStateMachine(finiteStateMachine);
        this.setName(name);
        this.custom = custom;
        return this;
    }

    public StateImpl initialize(FiniteStateMachine finiteStateMachine, boolean custom, String name, Stage stage) {
        this.initialize(finiteStateMachine, custom, name);
        this.stage.set(stage);
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
    public Optional<Stage> getStage() {
        return stage.getOptional();
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

    public boolean isObsolete() {
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
    @XmlTransient
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

    @Override
    public List<EndPointConfigurationReference> getOnEntryEndPointConfigurations() {
        return this.getEndPointConfigurationReferences(EndPointConfigurationReferenceImpl::isOnEntry);
    }

    @Override
    public List<EndPointConfigurationReference> getOnExitEndPointConfigurations() {
        return this.getEndPointConfigurationReferences(EndPointConfigurationReferenceImpl::isOnExit);
    }

    private List<ProcessReference> getProcessReferences(Predicate<? super ProcessReferenceImpl> predicate) {
        return this.processReferences
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private List<EndPointConfigurationReference> getEndPointConfigurationReferences(Predicate<? super EndPointConfigurationReferenceImpl> predicate) {
        return this.endPointConfigurationReferences
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    void addOnEntry(BpmProcessDefinition process) {
        this.processReferences.add(this.dataModel.getInstance(ProcessReferenceImpl.class).onEntry(this, process));
    }

    void addOnExit(BpmProcessDefinition process) {
        this.processReferences.add(this.dataModel.getInstance(ProcessReferenceImpl.class).onExit(this, process));
    }

    void addOnEntry(EndPointConfiguration endPointConfiguration) {
        this.endPointConfigurationReferences.add(this.dataModel.getInstance(EndPointConfigurationReferenceImpl.class).onEntry(this, endPointConfiguration));
    }

    void addOnExit(EndPointConfiguration endPointConfiguration) {
        this.endPointConfigurationReferences.add(this.dataModel.getInstance(EndPointConfigurationReferenceImpl.class).onExit(this, endPointConfiguration));
    }

    void removeOnEntry(BpmProcessDefinition process) {
        this.removeProcessReferences(process, ProcessReferenceImpl::isOnEntry);
    }

    void removeOnExit(BpmProcessDefinition process) {
        this.removeProcessReferences(process, ProcessReferenceImpl::isOnExit);
    }

    void removeOnEntry(EndPointConfiguration endPointConfiguration) {
        this.removeEndPointConfigurationReferences(endPointConfiguration, EndPointConfigurationReferenceImpl::isOnEntry);
    }

    void removeOnExit(EndPointConfiguration endPointConfiguration) {
        this.removeEndPointConfigurationReferences(endPointConfiguration, EndPointConfigurationReferenceImpl::isOnExit);
    }

    private void removeProcessReferences(BpmProcessDefinition process, Predicate<ProcessReferenceImpl> isEntryOrExit) {
        List<ProcessReferenceImpl> obsoleteReferences = this.processReferences
                .stream()
                .filter(isEntryOrExit)
                .filter(p -> p.matches(process))
                .collect(Collectors.toList());
        if (obsoleteReferences.isEmpty()) {
            throw new UnknownProcessReferenceException(this.thesaurus, this, process);
        } else {
            this.processReferences.removeAll(obsoleteReferences);
        }
    }

    private void removeEndPointConfigurationReferences(EndPointConfiguration endPointConfiguration, Predicate<EndPointConfigurationReferenceImpl> isEntryOrExit) {
        List<EndPointConfigurationReferenceImpl> obsoleteReferences = this.endPointConfigurationReferences
                .stream()
                .filter(isEntryOrExit)
                .filter(p -> p.matches(endPointConfiguration))
                .collect(Collectors.toList());
        if (obsoleteReferences.isEmpty()) {
            throw new UnknownProcessReferenceException(this.thesaurus, this, endPointConfiguration);
        } else {
            this.endPointConfigurationReferences.removeAll(obsoleteReferences);
        }
    }

    void prepareDelete() {
        this.processReferences.clear();
        this.endPointConfigurationReferences.clear();
    }

    void makeObsolete() {
        this.obsoleteTimestamp = this.clock.instant();
        this.prepareDelete();
        this.dataModel.update(this, Fields.OBSOLETE_TIMESTAMP.fieldName());
    }

    void update() {
        Save.UPDATE.save(this.dataModel, this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.getName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StateImpl that = (StateImpl) o;

        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}