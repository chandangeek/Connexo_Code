/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.impl.constraints.Unique;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link StandardStateTransitionEventType} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-05 (10:02)
 */
@Unique(message = "{" + MessageSeeds.Keys.UNIQUE_STANDARD_EVENT_TYPE + "}", groups = { Save.Create.class, Save.Update.class })
public class StandardStateTransitionEventTypeImpl extends StateTransitionEventTypeImpl implements StandardStateTransitionEventType {

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", groups = { Save.Create.class, Save.Update.class })
    private Reference<com.elster.jupiter.events.EventType> eventType = ValueReference.absent();

    @Inject
    public StandardStateTransitionEventTypeImpl(DataModel dataModel, Thesaurus thesaurus, ServerFiniteStateMachineService stateMachineService) {
        super(dataModel, thesaurus, stateMachineService);
    }

    StandardStateTransitionEventTypeImpl initialize(com.elster.jupiter.events.EventType eventType) {
        super.initialize(eventType != null ? eventType.getScope() : null);
        this.eventType.set(eventType);
        return this;
    }

    @Override
    public EventType getEventType() {
        /* get would be sufficient but custom java.validator components
         * that run alongside the validator for @IsPresent
         * will be calling this too, even when it does not have a value. */
        return this.eventType.orElse(null);
    }

    @Override
    public String getSymbol() {
        return this.getEventType().getTopic();
    }

    @Override
    public void update() {
        super.update();
    }

    void save() {
        super.save();
        EventType eventType = this.getEventType();
        eventType.enableForUseInStateMachines();
        eventType.update();
    }

    @Override
    public void delete() {
        EventType eventType = this.getEventType();
        super.delete();
        eventType.disableForUseInStateMachines();
        eventType.update();
    }

}