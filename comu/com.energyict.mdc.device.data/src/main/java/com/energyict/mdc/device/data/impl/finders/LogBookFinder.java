package com.energyict.mdc.device.data.impl.finders;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.LogBook;
import com.google.common.base.Optional;

/**
 * Copyrights EnergyICT
 * Date: 27/03/14
 * Time: 16:43
 */
public class LogBookFinder implements CanFindByLongPrimaryKey<LogBook>{

    private final DataModel dataModel;

    public LogBookFinder(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.LOADPROFILE;
    }

    @Override
    public Class<LogBook> valueDomain() {
        return LogBook.class;
    }

    @Override
    public Optional<LogBook> findByPrimaryKey(long id) {
        return this.dataModel.mapper(LogBook.class).getUnique("id", id);
    }

}