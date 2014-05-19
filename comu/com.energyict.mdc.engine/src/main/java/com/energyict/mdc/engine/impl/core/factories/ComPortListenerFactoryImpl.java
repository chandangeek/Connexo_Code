package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.MultiThreadedComPortListener;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.ServletInboundComPortListener;
import com.energyict.mdc.engine.impl.core.SingleThreadedComPortListener;
import com.energyict.mdc.engine.model.InboundComPort;

/**
 * Provides an implementation for the {@link ComPortListenerFactory}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:32)
 */
public class ComPortListenerFactoryImpl implements ComPortListenerFactory {

    private final ComServerDAO comServerDAO;
    private final DeviceCommandExecutor deviceCommandExecutor;

    public ComPortListenerFactoryImpl(ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor) {
        super();
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
    }

    @Override
    public ComPortListener newFor(InboundComPort comPort, ServiceProvider serviceProvider) {
        if (comPort.isActive()) {
            if (!comPort.isServletBased()) {
                switch (comPort.getNumberOfSimultaneousConnections()) {
                    case 0: {
                        return null;
                    }
                    case 1: {
                        return new SingleThreadedComPortListener(comPort, this.comServerDAO, this.deviceCommandExecutor, serviceProvider);
                    }
                    default: {
                        return new MultiThreadedComPortListener(comPort, this.comServerDAO, this.deviceCommandExecutor, serviceProvider);
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