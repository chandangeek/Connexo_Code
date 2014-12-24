package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComChannelBasedComPortListenerImpl;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.MultiThreadedComPortListener;
import com.energyict.mdc.engine.impl.core.ServletInboundComPortListener;
import com.energyict.mdc.engine.impl.core.SingleThreadedComPortListener;
import com.energyict.mdc.engine.config.InboundComPort;

import java.util.concurrent.ThreadFactory;

/**
 * Provides an implementation for the {@link ComPortListenerFactory}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:32)
 */
public class ComPortListenerFactoryImpl implements ComPortListenerFactory {

    private final ComServerDAO comServerDAO;
    private final DeviceCommandExecutor deviceCommandExecutor;
    private final ThreadFactory threadFactory;
    private final ComChannelBasedComPortListenerImpl.ServiceProvider  serviceProvider;

    public ComPortListenerFactoryImpl(ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ComChannelBasedComPortListenerImpl.ServiceProvider  serviceProvider) {
        super();
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.threadFactory = threadFactory;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public ComPortListener newFor(InboundComPort comPort) {
        if (comPort.isActive()) {
            if (!comPort.isServletBased()) {
                switch (comPort.getNumberOfSimultaneousConnections()) {
                    case 0: {
                        return null;
                    }
                    case 1: {
                        return new SingleThreadedComPortListener(comPort, this.comServerDAO, this.threadFactory, this.deviceCommandExecutor, serviceProvider);
                    }
                    default: {
                        return new MultiThreadedComPortListener(comPort, this.comServerDAO, this.deviceCommandExecutor, this.threadFactory, serviceProvider);
                    }
                }
            } else {
                return new ServletInboundComPortListener(comPort, this.comServerDAO, this.deviceCommandExecutor, serviceProvider);
            }
        } else {
            return null;
        }
    }

}