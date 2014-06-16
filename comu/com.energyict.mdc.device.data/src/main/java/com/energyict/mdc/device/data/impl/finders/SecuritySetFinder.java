package com.energyict.mdc.device.data.impl.finders;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.google.common.base.Optional;

/**
 * Copyrights EnergyICT
 * Date: 6/3/14
 * Time: 10:45 AM
 */
public class SecuritySetFinder implements CanFindByLongPrimaryKey<SecurityPropertySet> {

    private final DataModel dataModel;

    public SecuritySetFinder(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.SECURITY_SET;
    }

    @Override
    public Class<SecurityPropertySet> valueDomain() {
        return SecurityPropertySet.class;
    }

    @Override
    public Optional<SecurityPropertySet> findByPrimaryKey(long id) {
        return this.dataModel.mapper(SecurityPropertySet.class).getUnique("id", id);
    }

}