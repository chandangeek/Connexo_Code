/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.lifecycle.EvaluableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MicroCategoryTranslationKey;

import javax.inject.Inject;
import java.util.Optional;

public abstract class ConsolidatedServerMicroCheck implements ServerMicroCheck {

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
        return thesaurus.getFormat(MicroCheckTranslationKeys.MICRO_CHECK_NAME_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE).format();
    }

    @Override
    public String getDescription() {
        return thesaurus.getFormat(MicroCheckTranslationKeys.MICRO_CHECK_DESCRIPTION_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE).format();
    }

    @Override
    public String getCategoryName() {
        return thesaurus.getFormat(MicroCategoryTranslationKey.TRANSITION_ACTION_CHECK_CATEGORY_COMMUNICATION).format();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TranslatableServerMicroCheck that = (TranslatableServerMicroCheck) o;
        return getKey() != null ? getKey().equals(that.getKey()) : that.getKey() == null;

    }

    @Override
    public int hashCode() {
        return getKey() != null ? getKey().hashCode() : 0;
    }

    protected Optional<EvaluableMicroCheckViolation> violationFailed(TranslationKey failMessage, Object... args) {
        return Optional.of(new EvaluableMicroCheckViolation(this, this.thesaurus.getFormat(failMessage).format(args)));
    }
}