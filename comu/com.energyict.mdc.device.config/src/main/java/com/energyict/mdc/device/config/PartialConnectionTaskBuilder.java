package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.impl.PartialConnectionTask;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 14:33
 */
public interface PartialConnectionTaskBuilder<S, T extends ComPortPool, U extends PartialConnectionTask> {

    S pluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass);

    S comPortPool(T comPortPool);

    S name(String name);

    U build();
}
