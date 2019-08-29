/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface PartialConnectionTaskBuilder<S, T extends ComPortPool, U extends PartialConnectionTask> {

    S pluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass);

    S comPortPool(T comPortPool);

    S addProperty(String key, Object value);

    S name(String name);

    S setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties);

    S connectionFunction(ConnectionFunction connectionFunction);

    U build();
}
