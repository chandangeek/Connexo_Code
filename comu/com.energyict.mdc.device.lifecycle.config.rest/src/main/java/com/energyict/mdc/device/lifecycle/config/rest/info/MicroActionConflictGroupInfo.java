/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.ConflictGroupTranslationKey;

public class MicroActionConflictGroupInfo {
    public String id;
    public String name;
    public String description;

    public MicroActionConflictGroupInfo() {
    }

    public MicroActionConflictGroupInfo(String id, Thesaurus thesaurus) {
        this.id = id;
        this.name = thesaurus.getString(id, id);
        this.description = thesaurus.getString(ConflictGroupTranslationKey.Keys.TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY + id, id);
    }

}