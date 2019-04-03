/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheck;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.MicroCategoryTranslationKey;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class ConsolidatedServerMicroCheck implements ExecutableMicroCheck {

    private Thesaurus thesaurus;

    @Inject
    public final void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(MicroCheckTranslations.Name.MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE).format();
    }

    @Override
    public String getDescription() {
        return thesaurus.getFormat(MicroCheckTranslations.Description.MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE).format();
    }

    @Override
    public String getCategoryName() {
        return thesaurus.getFormat(MicroCategoryTranslationKey.TRANSITION_ACTION_CHECK_CATEGORY_COMMUNICATION).format();
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || o instanceof ConsolidatedServerMicroCheck
                && Objects.equals(getKey(), ((ConsolidatedServerMicroCheck) o).getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    @Override
    public Set<DefaultTransition> getRequiredDefaultTransitions() {
        return EnumSet.of(
                DefaultTransition.COMMISSION,
                DefaultTransition.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING,
                DefaultTransition.INSTALL_INACTIVE_WITHOUT_COMMISSIONING,
                DefaultTransition.INSTALL_AND_ACTIVATE,
                DefaultTransition.INSTALL_INACTIVE,
                DefaultTransition.ACTIVATE,
                DefaultTransition.DEACTIVATE);
    }

    protected Optional<ExecutableMicroCheckViolation> fail(MessageSeed failMessage, Object... args) {
        return Optional.of(new ExecutableMicroCheckViolation(this, this.thesaurus.getSimpleFormat(failMessage).format(args)));
    }
}
