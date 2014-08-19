package com.energyict.mdc.device.data.impl.finders;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.orm.DataModel;
import com.google.common.base.Optional;

/**
 * Serves as a <i>Finder</i> factory for {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-05-16 (09:47)
*/
public class ConnectionTaskFinder implements CanFindByLongPrimaryKey<ConnectionTask> {

    private final DataModel dataModel;

    public ConnectionTaskFinder(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.CONNECTION_TASK;
    }

    @Override
    public Class<ConnectionTask> valueDomain() {
        return ConnectionTask.class;
    }

    @Override
    public Optional<ConnectionTask> findByPrimaryKey(long id) {
        return dataModel.mapper(this.valueDomain()).getUnique("id", id);
    }

}