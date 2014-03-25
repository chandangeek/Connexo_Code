package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Currently defines what a <i>name</i> of a property
 *
 * Copyrights EnergyICT
 * Date: 21/03/14
 * Time: 14:35
 */
public class InfoTypeImpl implements InfoType {

    private final DataModel dataModel;
    private long id;
    private String name;

    @Inject
    public InfoTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void save() {
        Save.CREATE.save(this.dataModel, this);
    }

    InfoType initialize(String name) {
        this.name = name;
        return this;
    }
}
