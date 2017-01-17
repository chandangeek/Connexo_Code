package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultTransition;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategoryTranslationKeys;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

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
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getString(MicroCheckTranslationKeys.Keys.NAME_PREFIX + getKey(), getKey());
    }

    @Override
    public String getDescription() {
        return this.thesaurus.getString(MicroCheckTranslationKeys.Keys.MESSAGE_PREFIX + getKey(), getKey());
    }

    @Override
    public String getCategoryName() {
        return this.thesaurus.getString(MicroCategoryTranslationKeys.Keys.NAME_PREFIX + getCategory(), getCategory());
    }

    protected Optional<ExecutableMicroCheckViolation> fail(TranslationKey failMessage, Object... args) {
        return Optional.of(new ExecutableMicroCheckViolation(this, this.thesaurus.getFormat(failMessage).format(args)));
    }

    @Override
    public boolean isMandatoryForTransition(UsagePointState fromState, UsagePointState toState) {
        Set<DefaultTransition> candidates = getTransitionCandidates();
        return candidates != null
                && !candidates.isEmpty()
                && DefaultTransition.getDefaultTransition(fromState, toState).filter(candidates::contains).isPresent();
    }

    protected Set<DefaultTransition> getTransitionCandidates() {
        return EnumSet.noneOf(DefaultTransition.class);
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
