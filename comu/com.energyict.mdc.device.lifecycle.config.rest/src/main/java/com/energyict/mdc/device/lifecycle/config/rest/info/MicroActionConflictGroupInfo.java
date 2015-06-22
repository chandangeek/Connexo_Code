package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.energyict.mdc.device.lifecycle.config.rest.i18n.MicroCategoryTranslationKey;

import com.elster.jupiter.nls.Thesaurus;

public class MicroActionConflictGroupInfo {
    public String id;
    public String name;
    public String description;

    public MicroActionConflictGroupInfo() {
    }

    public MicroActionConflictGroupInfo(String id, Thesaurus thesaurus) {
        this.id = id;
        this.name = thesaurus.getString(id, id);
        this.description = thesaurus.getString(MicroCategoryTranslationKey.Keys.TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY + id, id);
    }

}