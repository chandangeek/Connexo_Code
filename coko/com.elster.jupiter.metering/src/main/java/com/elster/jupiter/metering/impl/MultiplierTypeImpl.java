package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Objects;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;

final class MultiplierTypeImpl implements MultiplierType {

    private final DataModel dataModel;
    private Thesaurus thesaurus;

    @SuppressWarnings("unused")
    private long id;
    @Size(max = NAME_LENGTH)
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

    void save() {
        dataModel.mapper(MultiplierType.class).persist(this);
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
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}