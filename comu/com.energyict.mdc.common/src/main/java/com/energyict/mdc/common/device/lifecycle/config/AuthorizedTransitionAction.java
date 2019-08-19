/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.common.device.lifecycle.config;

import com.elster.jupiter.fsm.StateTransition;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

@ProviderType
public interface AuthorizedTransitionAction extends AuthorizedAction {

    StateTransition getStateTransition();

    Set<MicroCheck> getChecks();

    Set<MicroAction> getActions();
}
