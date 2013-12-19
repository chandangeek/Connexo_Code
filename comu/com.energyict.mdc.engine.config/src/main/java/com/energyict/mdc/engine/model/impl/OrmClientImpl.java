package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;

public class OrmClientImpl implements OrmClient {

    private final DataModel dataModel;

    public OrmClientImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DataMapper<ComServer> getComServerFactory() {
        return dataModel.getDataMapper(ComServer.class, TableSpecs.MDCCOMSERVER.name());
    }

    @Override
    public DataMapper<ComPort> getComPortFactory() {
        return dataModel.getDataMapper(ComPort.class, TableSpecs.MDCCOMPORT.name());
    }
    
    @Override
    public DataMapper<ComPortPool> getComPortPoolFactory() {
        return dataModel.getDataMapper(ComPortPool.class, TableSpecs.MDCCOMPORTPOOL.name());
    }

    @Override
    public DataMapper<ComPortPoolMember> getComPortPoolMemberFactory() {
        return dataModel.getDataMapper(ComPortPoolMember.class, TableSpecs.MDCCOMPORTINPOOL.name());
    }
}
