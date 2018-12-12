/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.InboundComPortExecutor;

public interface InboundComPortExecutorFactory {

    /**
     * Creates an InboundComPortExecutor based on the given parameters.
     *
     * @param inboundComPort The InboundComPort
     * @param comServerDAO The ComServerDAO
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @return the newly created InboundComPortExecutor
     */
    InboundComPortExecutor create(InboundComPort inboundComPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor);

}
