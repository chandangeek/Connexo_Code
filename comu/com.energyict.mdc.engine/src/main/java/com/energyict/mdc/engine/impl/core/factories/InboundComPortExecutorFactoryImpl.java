/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.InboundComPortExecutor;
import com.energyict.mdc.engine.impl.core.InboundComPortExecutorImpl;

public class InboundComPortExecutorFactoryImpl implements InboundComPortExecutorFactory{

    private final InboundComPortExecutorImpl.ServiceProvider serviceProvider;

    public InboundComPortExecutorFactoryImpl(InboundComPortExecutorImpl.ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
    }

    @Override
    public InboundComPortExecutor create(InboundComPort inboundComPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor) {
        return new InboundComPortExecutorImpl(inboundComPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);
    }

}
