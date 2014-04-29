package com.energyict.mdc.masterdata.finders;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.google.common.base.Optional;

/**
 * Copyrights EnergyICT
 * Date: 29/04/14
 * Time: 9:00
 */
public class LoadProfileTypeFinder implements CanFindByLongPrimaryKey<LoadProfileType> {

    private final DataModel dataModel;

    public LoadProfileTypeFinder(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public FactoryIds registrationKey() {
        return FactoryIds.LOADPROFILE_TYPE;
    }

    @Override
    public Class<LoadProfileType> valueDomain() {
        return LoadProfileType.class;
    }

    @Override
    public Optional<LoadProfileType> findByPrimaryKey(long id) {
        return this.dataModel.mapper(LoadProfileType.class).getUnique("id", id);
    }
}
