/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.lifecycle.device.microchecks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheck;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Check if at least one LRN was set on the device
 */
public class LRNWasSet implements ExecutableMicroCheck {

    private final Thesaurus thesaurus;
    private final SAPCustomPropertySets sapCustomPropertySets;

    @Inject
    public LRNWasSet(Thesaurus thesaurus, SAPCustomPropertySets sapCustomPropertySets) {
        this.thesaurus = thesaurus;
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Override
    public String getKey() {
        return TranslationKeys.AT_LEAST_ONE_LRN_WAS_SET.getKey();
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.AT_LEAST_ONE_LRN_WAS_SET).format();
    }

    @Override
    public String getDescription() {
        return thesaurus.getFormat(TranslationKeys.AT_LEAST_ONE_LRN_WAS_SET_DESCRIPTION).format();
    }

    @Override
    public String getCategory() {
        return TranslationKeys.COMMUNICATION.name();
    }

    @Override
    public String getCategoryName() {
        return thesaurus.getFormat(TranslationKeys.COMMUNICATION).format();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
        return !anyLRN(device) ?
                Optional.of(new ExecutableMicroCheckViolation(this, this.thesaurus.getSimpleFormat(MessageSeeds.AT_LEAST_ONE_LRN_WAS_SET).format())) :
                Optional.empty();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.allOf(DefaultTransition.class);
    }

    private boolean anyLRN(Device device) {
            return sapCustomPropertySets.isAnyLrn(device.getId());
    }
}
