package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategoryTranslationKeys;

import javax.inject.Inject;

public abstract class TranslatableCheck implements ExecutableMicroCheck {
    private Thesaurus thesaurus;

    protected final Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Inject
    public final void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
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
        return this.thesaurus.getString(MicroCategoryTranslationKeys.Keys.NAME_PREFIX + getCategory(), getCategory());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TranslatableCheck that = (TranslatableCheck) o;
        return getKey() != null ? getKey().equals(that.getKey()) : that.getKey() == null;

    }

    @Override
    public int hashCode() {
        return getKey() != null ? getKey().hashCode() : 0;
    }
}
