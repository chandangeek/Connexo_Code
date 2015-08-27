package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroCategoryTranslationKey;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroCheckTranslationKey;

public abstract class ConsolidatedServerMicroCheck implements ServerMicroCheck {
    protected final Thesaurus thesaurus;

    public ConsolidatedServerMicroCheck(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return getTranslated(MicroCheckTranslationKey.MICRO_CHECK_NAME_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE);
    }

    @Override
    public String getDescription() {
        return getTranslated(MicroCheckTranslationKey.MICRO_CHECK_DESCRIPTION_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE);
    }

    @Override
    public String getCategoryName() {
        return getTranslated(MicroCategoryTranslationKey.TRANSITION_ACTION_CHECK_CATEGORY_COMMUNICATION);
    }

    protected String getTranslated(TranslationKey translationKey){
        return thesaurus.getString(translationKey.getKey(), translationKey.getDefaultFormat());
    }
}
