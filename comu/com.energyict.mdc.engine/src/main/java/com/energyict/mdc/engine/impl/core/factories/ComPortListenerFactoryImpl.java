/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.CoapInboundComPortListener;
import com.energyict.mdc.engine.impl.core.ComChannelBasedComPortListenerImpl;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.MultiThreadedComPortListener;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.ServletInboundComPortListener;
import com.energyict.mdc.engine.impl.core.SingleThreadedComPortListener;

/**
 * Provides an implementation for the {@link ComPortListenerFactory}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:32)
 */
public class ComPortListenerFactoryImpl implements ComPortListenerFactory {

    private final ComServerDAO comServerDAO;
    private final DeviceCommandExecutor deviceCommandExecutor;
    private final ComChannelBasedComPortListenerImpl.ServiceProvider serviceProvider;

    public ComPortListenerFactoryImpl(ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ComChannelBasedComPortListenerImpl.ServiceProvider serviceProvider) {
        super();
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public ComPortListener newFor(RunningComServer runningComServer, InboundComPort comPort) {
        if (comPort.isActive() && comPort.isCoapBased()) {
            return new CoapInboundComPortListener(runningComServer, comPort, this.comServerDAO, this.deviceCommandExecutor, this.serviceProvider);
        }
        if (comPort.isActive() && comPort.isServletBased()) {
            return new ServletInboundComPortListener(runningComServer, comPort, this.comServerDAO, this.deviceCommandExecutor, this.serviceProvider);
        }
        if (comPort.isActive() && comPort.getNumberOfSimultaneousConnections() == 1 && !comPort.isUDPBased()) {
            return new SingleThreadedComPortListener(runningComServer, comPort, this.deviceCommandExecutor, this.serviceProvider);
        }
        if (comPort.isActive() && comPort.getNumberOfSimultaneousConnections() > 0) {
            return new MultiThreadedComPortListener(runningComServer, comPort, this.deviceCommandExecutor, this.serviceProvider);
        }
        return null;
    }
}