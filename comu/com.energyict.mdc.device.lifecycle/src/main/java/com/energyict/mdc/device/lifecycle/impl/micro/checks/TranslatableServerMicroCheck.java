package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroCategoryTranslationKey;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroCheckTranslationKey;

import com.elster.jupiter.nls.Thesaurus;

public abstract class TranslatableServerMicroCheck implements ServerMicroCheck {
    protected final Thesaurus thesaurus;

    public TranslatableServerMicroCheck(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected abstract MicroCheck getMicroCheck();

    @Override
    public String getName() {
        MicroCheck microCheck = getMicroCheck();
        return MicroCheckTranslationKey
                .getNameFor(microCheck)
                .map(thesaurus::getFormat)
                .map(nlsMessageFormat -> nlsMessageFormat.format())
                .orElse(microCheck.name());
    }

    @Override
    public String getDescription() {
        MicroCheck microCheck = getMicroCheck();
        return MicroCheckTranslationKey
                .getDescriptionFor(microCheck)
                .map(thesaurus::getFormat)
                .map(nlsMessageFormat -> nlsMessageFormat.format())
                .orElse(microCheck.name());
    }

    @Override
    public String getCategoryName() {
        MicroCategory microCategory = getMicroCheck().getCategory();
        return MicroCategoryTranslationKey
                .getCategory(microCategory)
                .map(thesaurus::getFormat)
                .map(nlsMessageFormat -> nlsMessageFormat.format())
                .orElse(microCategory.name());
    }

}