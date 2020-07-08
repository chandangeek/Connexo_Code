/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycleUpdater;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.EventType;
import com.energyict.mdc.device.lifecycle.config.impl.constraints.Unique;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceLifeCycle} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:30)
 */
@Unique(message = "{" + MessageSeeds.Keys.UNIQUE_DEVICE_LIFE_CYCLE_NAME + "}", groups = {Save.Create.class, Save.Update.class})
@XmlRootElement
public class DeviceLifeCycleImpl implements DeviceLifeCycle {

    public enum Fields {
        NAME("name"),
        OBSOLETE_TIMESTAMP("obsoleteTimestamp"),
        STATE_MACHINE("stateMachine"),
        ACTIONS("actions");

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
    private final EventService eventService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(blacklisted = {'<', '>'})
    private String name;
    @SuppressWarnings("unused")
    private Instant obsoleteTimestamp;
    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", groups = {Save.Create.class, Save.Update.class})
    private Reference<FiniteStateMachine> stateMachine = ValueReference.absent();
    @Valid
    private List<AuthorizedAction> actions = new ArrayList<>();
    private List<AuthorizedActionImpl> updated = new ArrayList<>();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public DeviceLifeCycleImpl(DataModel dataModel, Thesaurus thesaurus, Clock clock, EventService eventService,
                               DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.eventService = eventService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    public DeviceLifeCycleImpl initialize(String name, FiniteStateMachine stateMachine) {
        setName(name);
        this.stateMachine.set(stateMachine);
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        if (DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey().equals(this.name)) {
            return this.thesaurus.getFormat(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME).format();
        }
        return this.name;
    }

    void setName(String name) {
        if (!Checks.is(name).emptyOrOnlyWhiteSpace()) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isObsolete() {
        return this.obsoleteTimestamp != null;
    }

    @Override
    public Instant getObsoleteTimestamp() {
        return obsoleteTimestamp;
    }

    @Override
    public FiniteStateMachine getFiniteStateMachine() {
        return this.stateMachine.get();
    }

    @Override
    public Instant getMaximumFutureEffectiveTimestamp() {
        return this.clock.instant().plusMillis(deviceLifeCycleConfigurationService.getMaximumFutureEffectiveTimeShift().getMilliSeconds());
    }

    @Override
    public Instant getMaximumPastEffectiveTimestamp() {
        return this.clock.instant().minusMillis(deviceLifeCycleConfigurationService.getMaximumPastEffectiveTimeShift().getMilliSeconds());
    }

    @Override
    public List<AuthorizedAction> getAuthorizedActions() {
        return Collections.unmodifiableList(this.actions);
    }

    @Override
    public List<AuthorizedAction> getAuthorizedActions(State state) {
        return this.actions
                .stream()
                .filter(a -> state.getId() == a.getState().getId())
                .collect(Collectors.toList());
    }

    void add(AuthorizedAction action) {
        this.actions.add(action);
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

    void updated(AuthorizedActionImpl action) {
        this.updated.add(action);
    }

    @Override
    public void save() {
        try {
            this.updated.forEach(AuthorizedActionImpl::save);
            Save.action(this.id).save(this.dataModel, this);
            eventService.postEvent(EventType.DEVICE_LIFECYCLE_UPDATE.topic(), this);
        } finally {
            this.updated = new ArrayList<>();
        }
    }

    @Override
    public void makeObsolete() {
        this.obsoleteTimestamp = this.clock.instant();
        this.deleteActions();
        this.dataModel.update(this, Fields.OBSOLETE_TIMESTAMP.fieldName());
    }

    @Override
    public void delete() {
        this.deleteActions();
        this.dataModel.remove(this);
    }

    private void deleteActions() {
        this.actions.clear();
    }

    @Override
    public DeviceLifeCycleUpdater startUpdate() {
        return new DeviceLifeCycleUpdaterImpl(this.dataModel, this);
    }

    AuthorizedTransitionActionImpl findActionFor(StateTransition transition) {
        return this.actions
                .stream()
                .filter(each -> each instanceof AuthorizedTransitionAction)
                .map(AuthorizedTransitionActionImpl.class::cast)
                .filter(each -> each.getStateTransition().getId() == transition.getId())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No authorized action for state transition with id " + transition.getId()));
    }

    void removeTransitionAction(StateTransition transition) {
        AuthorizedTransitionAction transitionAction = this.findActionFor(transition);
        this.actions.remove(transitionAction);
        eventService.postEvent(EventType.DEVICE_LIFECYCLE_TRASITION_DELETE.topic(), Pair.of(this, transition));
    }

}