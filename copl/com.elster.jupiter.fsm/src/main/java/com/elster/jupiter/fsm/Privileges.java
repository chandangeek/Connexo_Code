/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;
/**
 * Models the privileges of the finite state machine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (17:00)
 */
@ProviderType
public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_FSM("finiteStateMachineAdministration.finiteStateMachineAdministrations", "Finite state machine"),
    RESOURCE_FSM_DESCRIPTION("finiteStateMachineAdministration.finiteStateMachineAdministrations.description", "Manage finite state machine"),

    //Privileges
    VIEW_FINITE_STATE_MACHINES(Constants.VIEW_FINITE_STATE_MACHINES, "View"),
    CONFIGURE_FINITE_STATE_MACHINES(Constants.CONFIGURE_FINITE_STATE_MACHINES, "Configure");

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
        public String VIEW_FINITE_STATE_MACHINES = "privilege.view.finiteStateMachines";
        public String CONFIGURE_FINITE_STATE_MACHINES = "privilege.configure.finiteStateMachines";
    }
}
