package com.energyict.mdc.device.data.impl.finders;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethod;

import com.elster.jupiter.orm.DataModel;
import com.google.common.base.Optional;

/**
 * Serves as a <i>Finder</i> factory for {@link com.energyict.mdc.device.data.impl.tasks.ConnectionMethod}s.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-05-16 (09:47)
*/
public class ConnectionMethodFinder implements CanFindByLongPrimaryKey<ConnectionMethod> {

    private final DataModel dataModel;

    public ConnectionMethodFinder(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.CONNECTION_METHOD;
    }

    @Override
    public Class<ConnectionMethod> valueDomain() {
        return ConnectionMethod.class;
    }

    @Override
    public Optional<ConnectionMethod> findByPrimaryKey(long id) {
        return dataModel.mapper(this.valueDomain()).getUnique("id", id);
    }

}