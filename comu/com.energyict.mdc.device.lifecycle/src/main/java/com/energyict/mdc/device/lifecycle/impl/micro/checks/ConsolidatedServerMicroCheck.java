/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheck;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.impl.MicroCategoryTranslationKey;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

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

    protected Optional<ExecutableMicroCheckViolation> fail(MessageSeed failMessage, Object... args) {
        return Optional.of(new ExecutableMicroCheckViolation(this, this.thesaurus.getFormat(failMessage).format(args)));
    }
}
