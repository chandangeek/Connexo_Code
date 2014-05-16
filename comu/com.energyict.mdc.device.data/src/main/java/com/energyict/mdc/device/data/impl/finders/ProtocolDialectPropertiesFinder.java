package com.energyict.mdc.device.data.impl.finders;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.ProtocolDialectProperties;

import com.elster.jupiter.orm.DataModel;
import com.google.common.base.Optional;

/**
 * Serves as a <i>Finder</i> factory for {@link com.energyict.mdc.device.data.ProtocolDialectProperties}.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-05-16 (09:49)
*/
public class ProtocolDialectPropertiesFinder implements CanFindByLongPrimaryKey<ProtocolDialectProperties> {

    private final DataModel dataModel;

    public ProtocolDialectPropertiesFinder(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.DEVICE_PROTOCOL_DIALECT;
    }

    @Override
    public Class<ProtocolDialectProperties> valueDomain() {
        return ProtocolDialectProperties.class;
    }

    @Override
    public Optional<ProtocolDialectProperties> findByPrimaryKey(long id) {
        return dataModel.mapper(this.valueDomain()).getUnique("id", id);
    }

}