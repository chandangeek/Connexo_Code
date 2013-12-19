package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataMapper;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;

public interface OrmClient {
    public DataMapper<ComServer> getComServerFactory();
    public DataMapper<ComPort> getComPortFactory();
    public DataMapper<ComPortPool> getComPortPoolFactory();
    public DataMapper<ComPortPoolMember> getComPortPoolMemberFactory();
}
