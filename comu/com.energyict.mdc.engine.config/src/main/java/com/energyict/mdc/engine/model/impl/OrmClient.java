package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataMapper;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;

public interface OrmClient {
    public DataMapper<ServerComServer> getComServerDataMapper();
    public DataMapper<ComPort> getComPortDataMapper();
    public DataMapper<ComPortPool> getComPortPoolDataMapper();
    public DataMapper<ComPortPoolMember> getComPortPoolMemberDataMapper();
}
