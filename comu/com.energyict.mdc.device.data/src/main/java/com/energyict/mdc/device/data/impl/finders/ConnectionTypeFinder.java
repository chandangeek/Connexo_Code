package com.energyict.mdc.device.data.impl.finders;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.Optional;

public class ConnectionTypeFinder implements CanFindByLongPrimaryKey<ConnectionTypePluggableClass> {

    private final ProtocolPluggableService protocolPluggableService;

    public ConnectionTypeFinder(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.CONNECTION_TYPE;
    }

    @Override
    public Class<ConnectionTypePluggableClass> valueDomain() {
        return ConnectionTypePluggableClass.class;
    }

    @Override
    public Optional<ConnectionTypePluggableClass> findByPrimaryKey(long id) {
        return this.protocolPluggableService.findConnectionTypePluggableClass(id);
    }
}
