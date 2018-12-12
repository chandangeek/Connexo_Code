/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.favorites.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.favorites.LabelCategory;
import com.energyict.mdc.favorites.impl.MessageSeeds.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.DUPLICATE_LABEL_CATEGORY + "}")
public class LabelCategoryImpl implements LabelCategory, Serializable {

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;

    LabelCategoryImpl() {
        super();
    }

    LabelCategoryImpl init(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }
}
