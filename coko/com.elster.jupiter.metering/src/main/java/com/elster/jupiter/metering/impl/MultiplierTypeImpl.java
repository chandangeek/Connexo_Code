package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.Objects;

final class MultiplierTypeImpl implements MultiplierType {

    private final DataModel dataModel;

    private String name;

    @Inject
    MultiplierTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    MultiplierTypeImpl init(String name) {
        this.name = name;
        return this;
    }

    static MultiplierTypeImpl from(DataModel dataModel, String name) {
        return new MultiplierTypeImpl(dataModel).init(name);
    }

    @Override
    public String getName() {
        return name;
    }

    void save() {
        dataModel.mapper(MultiplierType.class).persist(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiplierTypeImpl that = (MultiplierTypeImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
