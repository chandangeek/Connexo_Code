package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactory;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactoryImpl;

/**
 * Provides an implementation for the {@link ComPortListener} interface
 * for an {@link InboundComPort} that supports one connection at a time.
 * In each run loop, changes to the InboundComPort will be monitored
 * as well as listen for incoming connections.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (11:27)
 */
public class SingleThreadedComPortListener extends ComChannelBasedComPortListenerImpl {

    private InboundComPortExecutorFactory inboundComPortExecutorFactory;

    public SingleThreadedComPortListener(InboundComPort comPort, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        this(comPort, deviceCommandExecutor, serviceProvider, new InboundComPortExecutorFactoryImpl(serviceProvider));
    }

    public SingleThreadedComPortListener(
                InboundComPort comPort,
                DeviceCommandExecutor deviceCommandExecutor,
                ServiceProvider serviceProvider,
                InboundComPortExecutorFactory inboundComPortExecutorFactory) {
        super(comPort, deviceCommandExecutor, serviceProvider);
        this.inboundComPortExecutorFactory = inboundComPortExecutorFactory;
    }

    @Override
    protected void doRun() {
        ComPortRelatedComChannel comChannel = listen();
        handleInboundDeviceProtocol(comChannel);
        /*
       Else no accept within the configured TimeOut, but this allows us to check for any changes
        */
    }

    /**
     * Properly create, initialize and execute the InboundDeviceProtocol.
     *
     * @param comChannel the CommunicationChannel which can be used to transfer bits and bytes over to the Device
     */
    protected void handleInboundDeviceProtocol(ComPortRelatedComChannel comChannel) {
        this.inboundComPortExecutorFactory.create(getServerInboundComPort(), getComServerDAO(), getDeviceCommandExecutor()).execute(comChannel);
    }

}