/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComPortPoolMember;
import com.energyict.mdc.engine.config.ComServer;

public class OrmClientImpl implements OrmClient {

    private final DataModel dataModel;

    public OrmClientImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DataMapper<ComServer> getComServerDataMapper() {
        return dataModel.mapper(ComServer.class);
    }

    @Override
    public DataMapper<ComPort> getComPortDataMapper() {
        return dataModel.mapper(ComPort.class);
    }

    @Override
    public DataMapper<ComPortPool> getComPortPoolDataMapper() {
        return dataModel.mapper(ComPortPool.class);
    }

    @Override
    public DataMapper<ComPortPoolMember> getComPortPoolMemberDataMapper() {
        return dataModel.mapper(ComPortPoolMember.class);
    }
}
