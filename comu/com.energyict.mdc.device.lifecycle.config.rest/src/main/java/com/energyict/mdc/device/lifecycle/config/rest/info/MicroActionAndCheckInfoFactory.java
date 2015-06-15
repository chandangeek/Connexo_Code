package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.MessageSeeds;

import javax.inject.Inject;

public class MicroActionAndCheckInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public MicroActionAndCheckInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public MicroActionAndCheckInfo required(MicroAction microAction){
        MicroActionAndCheckInfo info = common(microAction);
        info.isRequired = true;
        info.checked = true;
        return info;
    }

    public MicroActionAndCheckInfo optional(MicroAction microAction){
        MicroActionAndCheckInfo info = common(microAction);
        info.isRequired = false;
        return info;
    }

    private MicroActionAndCheckInfo common(MicroAction microAction){
        MicroActionAndCheckInfo info = new MicroActionAndCheckInfo();
        if (microAction != null) {
            info.key = microAction.name();
            info.name = thesaurus.getString(MessageSeeds.Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + microAction.name(), microAction.name());
            info.description = thesaurus.getString(MessageSeeds.Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + microAction.name(), microAction.name());
            info.category = new IdWithNameInfo();
            info.category.id = microAction.getCategory().name();
            info.category.name = thesaurus.getString(MessageSeeds.Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + microAction.getCategory().name(), microAction.getCategory().name());
            if (microAction.getSubCategoryKey() != null){
                info.subCategory = new IdWithNameInfo();
                info.subCategory.id = microAction.getSubCategoryKey();
                info.subCategory.name = thesaurus.getString(microAction.getSubCategoryKey(), microAction.getSubCategoryKey());
            }
        }
        return info;
    }
}
