package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroActionTranslationKey;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroCategoryTranslationKey;

import com.elster.jupiter.nls.Thesaurus;

public abstract class TranslatableServerMicroAction implements ServerMicroAction {
    protected final Thesaurus thesaurus;

    public TranslatableServerMicroAction(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected abstract MicroAction getMicroAction();

    @Override
    public String getName() {
        MicroAction microAction = getMicroAction();
        return MicroActionTranslationKey
                .getNameFor(microAction)
                .map(thesaurus::getFormat)
                .map(nlsMessageFormat -> nlsMessageFormat.format())
                .orElse(microAction.name());
    }

    @Override
    public String getDescription() {
        MicroAction microAction = getMicroAction();
        return MicroActionTranslationKey
                .getDescriptionFor(microAction)
                .map(thesaurus::getFormat)
                .map(nlsMessageFormat -> nlsMessageFormat.format())
                .orElse(microAction.name());
    }

    @Override
    public String getCategoryName() {
        MicroCategory microCategory = getMicroAction().getCategory();
        return MicroCategoryTranslationKey
                .getCategory(microCategory)
                .map(thesaurus::getFormat)
                .map(nlsMessageFormat -> nlsMessageFormat.format())
                .orElse(microCategory.name());
    }

}