/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.MultiThreadedScheduledComPort;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.impl.core.ScheduledComPortImpl;
import com.energyict.mdc.engine.impl.core.SingleThreadedScheduledComPort;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Provides an implmementation for the {@link ScheduledComPortFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:24)
 */
public class ScheduledComPortFactoryImpl implements ScheduledComPortFactory {

    private ComServerDAO comServerDAO;
    private DeviceCommandExecutor deviceCommandExecutor;
    private ThreadFactory threadFactory;
    private final ScheduledComPortImpl.ServiceProvider serviceProvider;

    ScheduledComPortFactoryImpl(ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ScheduledComPortImpl.ServiceProvider serviceProvider) {
        this(comServerDAO, deviceCommandExecutor, Executors.defaultThreadFactory(), serviceProvider);
    }

    public ScheduledComPortFactoryImpl(ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ScheduledComPortImpl.ServiceProvider serviceProvider) {
        super();
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.threadFactory = threadFactory;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public ScheduledComPort newFor(RunningComServer runningComServer, OutboundComPort comPort) {
        if (comPort.isActive()) {
            switch (comPort.getNumberOfSimultaneousConnections()) {
                case 0: {
                    return null;
                }
                case 1: {
                    return new SingleThreadedScheduledComPort(runningComServer, comPort, this.comServerDAO, this.deviceCommandExecutor, this.threadFactory, this.serviceProvider);
                }
                default: {
                    return new MultiThreadedScheduledComPort(runningComServer, comPort, this.comServerDAO, this.deviceCommandExecutor, this.threadFactory, this.serviceProvider);
                }
            }
        }
        else {
            return null;
        }
    }

}