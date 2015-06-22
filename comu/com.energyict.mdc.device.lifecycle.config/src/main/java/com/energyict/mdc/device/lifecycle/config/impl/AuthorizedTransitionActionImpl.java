package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import java.util.EnumSet;
import java.util.Set;

/**
 * Serves as the root of the class hierarchy that will implement
 * the {@link AuthorizedTransitionAction} interface hierarchy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (13:23)
 */
public abstract class AuthorizedTransitionActionImpl extends AuthorizedActionImpl implements AuthorizedTransitionAction {

    private final Thesaurus thesaurus;

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", groups = { Save.Create.class, Save.Update.class })
    private Reference<StateTransition> stateTransition = ValueReference.absent();
    @SuppressWarnings("unused")
    private long checkBits;
    private EnumSet<MicroCheck> checks = EnumSet.noneOf(MicroCheck.class);
    @SuppressWarnings("unused")
    private long actionBits;
    private EnumSet<MicroAction> actions = EnumSet.noneOf(MicroAction.class);

    protected AuthorizedTransitionActionImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    @Override
    public void postLoad() {
        super.postLoad();
        this.postLoadChecksEnumSet();
        this.postLoadActionsEnumSet();
    }

    private void postLoadChecksEnumSet() {
        int mask = 1;
        for (MicroCheck microCheck : MicroCheck.values()) {
            if ((this.checkBits & mask) != 0) {
                // The bit corresponding to the current microCheck is set so add it to the set.
                this.checks.add(microCheck);
            }
            mask = mask * 2;
        }
    }

    private void postLoadActionsEnumSet() {
        int mask = 1;
        for (MicroAction microAction : MicroAction.values()) {
            if ((this.actionBits & mask) != 0) {
                // The bit corresponding to the current microAction is set so add it to the set.
                this.actions.add(microAction);
            }
            mask = mask * 2;
        }
    }

    @Override
    public String getName() {
        return getStateTransition().getName(this.thesaurus);
    }

    @Override
    public StateTransition getStateTransition() {
        return this.stateTransition.get();
    }

    protected void setStateTransition(StateTransition stateTransition) {
        this.stateTransition.set(stateTransition);
    }

    @Override
    public State getState() {
        return this.getStateTransition().getFrom();
    }

    @Override
    public Set<MicroCheck> getChecks() {
        return EnumSet.copyOf(this.checks);
    }

    void clearChecks() {
        this.checkBits = 0;
        this.checks = EnumSet.noneOf(MicroCheck.class);
    }

    void add(MicroCheck check) {
        this.checkBits |= (1L << check.ordinal());
        this.checks.add(check);
    }

    @Override
    public Set<MicroAction> getActions() {
        return EnumSet.copyOf(this.actions);
    }

    void clearActions() {
        this.actionBits = 0;
        this.actions = EnumSet.noneOf(MicroAction.class);
    }

    void add(MicroAction action) {
        this.actionBits |= (1L << action.ordinal());
        this.actions.add(action);
    }

}