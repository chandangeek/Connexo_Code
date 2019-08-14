/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.orm.DataMapper;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComPortPoolMember;
import com.energyict.mdc.common.comserver.ComServer;

public interface OrmClient {
    public DataMapper<ComServer> getComServerDataMapper();
    public DataMapper<ComPort> getComPortDataMapper();
    public DataMapper<ComPortPool> getComPortPoolDataMapper();
    public DataMapper<ComPortPoolMember> getComPortPoolMemberDataMapper();
}
