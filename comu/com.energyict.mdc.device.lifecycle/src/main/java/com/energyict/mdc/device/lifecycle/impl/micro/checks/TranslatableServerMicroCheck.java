/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.impl.MicroCategoryTranslationKey;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheck;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public abstract class TranslatableServerMicroCheck implements ExecutableMicroCheck {

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
        return this.thesaurus.getString(MicroCheckTranslations.NAME_PREFIX + getKey(), getKey());
    }

    @Override
    public String getDescription() {
        return this.thesaurus.getString(MicroCheckTranslations.DESCRIPTION_PREFIX + getKey(), getKey());
    }

    @Override
    public String getCategoryName() {
        return this.thesaurus.getString(MicroCategoryTranslationKey.Keys.NAME_PREFIX + getCategory(), getCategory());
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || o instanceof TranslatableServerMicroCheck
                && Objects.equals(getKey(), ((TranslatableServerMicroCheck) o).getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    protected Optional<ExecutableMicroCheckViolation> fail(MessageSeed failMessage, Object... args) {
        return Optional.of(new ExecutableMicroCheckViolation(this, this.thesaurus.getFormat(failMessage).format(args)));
    }
}
