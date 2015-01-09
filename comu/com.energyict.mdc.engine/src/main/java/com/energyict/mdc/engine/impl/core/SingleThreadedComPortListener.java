package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactory;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactoryImpl;
import com.energyict.mdc.engine.config.InboundComPort;

import java.util.concurrent.ThreadFactory;

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

    public SingleThreadedComPortListener(InboundComPort comPort, ComServerDAO comServerDAO, ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        this(comPort, comServerDAO, threadFactory, deviceCommandExecutor, new InboundComPortExecutorFactoryImpl(serviceProvider), serviceProvider);
    }

    public SingleThreadedComPortListener(
                        InboundComPort comPort,
                        ComServerDAO comServerDAO,
                        ThreadFactory threadFactory,
                        DeviceCommandExecutor deviceCommandExecutor,
                        InboundComPortExecutorFactory inboundComPortExecutorFactory,
                        ServiceProvider serviceProvider) {
        this(comPort,
                comServerDAO,
                threadFactory,
                deviceCommandExecutor,
                inboundComPortExecutorFactory,
                new InboundComPortConnectorFactoryImpl(
                        serviceProvider.serialAtComponentService(),
                        serviceProvider.socketService(),
                        serviceProvider.hexService(),
                        serviceProvider.clock())
        );
    }

    public SingleThreadedComPortListener(
                InboundComPort comPort,
                ComServerDAO comServerDAO,
                ThreadFactory threadFactory,
                DeviceCommandExecutor deviceCommandExecutor,
                InboundComPortExecutorFactory inboundComPortExecutorFactory,
                InboundComPortConnectorFactory inboundComPortConnectorFactory) {
        super(comPort,
                comServerDAO,
                inboundComPortConnectorFactory,
                threadFactory,
                deviceCommandExecutor);
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

    @Override
    protected void applyChangesForNewComPort(InboundComPort inboundComPort) {
        // nothing to change.
        // the only thing that can change is the inboundDiscoveryProtocol and that is directly fetched from the ComPort
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