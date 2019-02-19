/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.lifecycle.EvaluableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.impl.MicroCategoryTranslationKey;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import javax.inject.Inject;
import java.util.Optional;

public abstract class TranslatableServerMicroCheck implements ServerMicroCheck {

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
        return this.thesaurus.getString(MicroCheckTranslationKeys.Keys.NAME_PREFIX + getKey(), getKey());
    }

    @Override
    public String getDescription() {
        return this.thesaurus.getString(MicroCheckTranslationKeys.Keys.DESCRIPTION_PREFIX + getKey(), getKey());
    }

    @Override
    public String getCategoryName() {
        return this.thesaurus.getString(MicroCategoryTranslationKey.Keys.NAME_PREFIX + getCategory(), getCategory());
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