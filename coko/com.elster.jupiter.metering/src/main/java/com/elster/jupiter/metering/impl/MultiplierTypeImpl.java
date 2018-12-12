/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.constraints.Size;

@UniqueTranslation(groups = {Save.Create.class, Save.Update.class})
final class MultiplierTypeImpl implements MultiplierType {

    private final DataModel dataModel;
    private Thesaurus thesaurus;

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    private boolean nameIsKey;

    @Inject
    MultiplierTypeImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    MultiplierTypeImpl initWithCustomName(String name) {
        this.name = name;
        this.nameIsKey = false;
        return this;
    }

    MultiplierTypeImpl initWithNlsNameKey(String nameKey) {
        this.name = nameKey;
        this.nameIsKey = true;
        return this;
    }

    MultiplierTypeImpl init(StandardType standardType) {
        return initWithNlsNameKey(standardType.translationKey());
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        if (this.nameIsKey) {
            return this.thesaurus.getFormat(new SimpleTranslationKey(this.name, this.name)).format();
        } else {
            return name;
        }
    }

    String name() {
        return name;
    }

    boolean nameIsKey() {
        return nameIsKey;
    }

    void save() {
        Save.CREATE.validate(this.dataModel, this);
        this.dataModel.mapper(MultiplierType.class).persist(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultiplierTypeImpl that = (MultiplierTypeImpl) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }

}