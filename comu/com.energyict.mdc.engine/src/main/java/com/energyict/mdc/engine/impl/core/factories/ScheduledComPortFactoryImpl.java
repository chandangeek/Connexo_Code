package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.MultiThreadedScheduledComPort;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.SingleThreadedScheduledComPort;
import com.energyict.mdc.engine.model.OutboundComPort;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Provides an implmementation for the {@link ScheduledComPortFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:24)
 */
public class ScheduledComPortFactoryImpl implements ScheduledComPortFactory {

    private final ServiceProvider serviceProvider;

    private ComServerDAO comServerDAO;
    private DeviceCommandExecutor deviceCommandExecutor;
    private ThreadFactory threadFactory;

    public ScheduledComPortFactoryImpl(ServiceProvider serviceProvider, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor) {
        this(serviceProvider, comServerDAO, deviceCommandExecutor, Executors.defaultThreadFactory());
    }

    public ScheduledComPortFactoryImpl(ServiceProvider serviceProvider, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory) {
        super();
        this.serviceProvider = serviceProvider;
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.threadFactory = threadFactory;
    }

    @Override
    public ScheduledComPort newFor(OutboundComPort comPort, ServiceProvider serviceProvider) {
        if (comPort.isActive()) {
            switch (comPort.getNumberOfSimultaneousConnections()) {
                case 0: {
                    return null;
                }
                case 1: {
                    return new SingleThreadedScheduledComPort(comPort, this.comServerDAO, this.deviceCommandExecutor, this.threadFactory, serviceProvider);
                }
                default: {
                    return new MultiThreadedScheduledComPort(comPort, this.comServerDAO, this.deviceCommandExecutor, this.threadFactory, serviceProvider);
                }
            }
        }
        else {
            return null;
        }
    }

}