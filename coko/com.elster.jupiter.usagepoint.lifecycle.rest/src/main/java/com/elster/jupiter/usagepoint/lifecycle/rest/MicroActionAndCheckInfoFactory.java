/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;

public class MicroActionAndCheckInfoFactory {

    public MicroActionAndCheckInfo required(MicroAction microAction) {
        MicroActionAndCheckInfo info = common(microAction);
        info.isRequired = true;
        info.checked = true;
        return info;
    }

    public MicroActionAndCheckInfo optional(MicroAction microAction) {
        MicroActionAndCheckInfo info = common(microAction);
        info.isRequired = false;
        return info;
    }

    private MicroActionAndCheckInfo common(MicroAction microAction) {
        MicroActionAndCheckInfo info = new MicroActionAndCheckInfo();
        if (microAction != null) {
            info.key = microAction.getKey();
            info.name = microAction.getName();
            info.description = microAction.getDescription();
            info.category = new IdWithNameInfo();
            info.category.id = microAction.getCategory();
            info.category.name = microAction.getCategoryName();
        }
        return info;
    }

    public MicroActionAndCheckInfo required(MicroCheck microCheck) {
        MicroActionAndCheckInfo info = common(microCheck);
        info.isRequired = true;
        info.checked = true;
        return info;
    }

    public MicroActionAndCheckInfo optional(MicroCheck microCheck) {
        MicroActionAndCheckInfo info = common(microCheck);
        info.isRequired = false;
        return info;
    }

    private MicroActionAndCheckInfo common(MicroCheck microCheck) {
        MicroActionAndCheckInfo info = new MicroActionAndCheckInfo();
        if (microCheck != null) {
            info.key = microCheck.getKey();
            info.name = microCheck.getName();
            info.description = microCheck.getDescription();
            info.category = new IdWithNameInfo();
            info.category.id = microCheck.getCategory();
            info.category.name = microCheck.getCategoryName();
        }
        return info;
    }
}