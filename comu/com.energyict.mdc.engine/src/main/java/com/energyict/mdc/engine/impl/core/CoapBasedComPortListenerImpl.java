/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;

import java.time.Clock;
import java.util.concurrent.ThreadFactory;

public abstract class CoapBasedComPortListenerImpl extends ComPortListenerImpl {

    protected CoapBasedComPortListenerImpl(RunningComServer runningComServer, InboundComPort comPort, Clock clock, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super(runningComServer, comPort, clock, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    protected CoapBasedComPortListenerImpl(RunningComServer runningComServer, InboundComPort comPort, Clock clock, ComServerDAO comServerDAO, ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super(runningComServer, comPort, clock, comServerDAO, threadFactory, deviceCommandExecutor, serviceProvider);
    }
}