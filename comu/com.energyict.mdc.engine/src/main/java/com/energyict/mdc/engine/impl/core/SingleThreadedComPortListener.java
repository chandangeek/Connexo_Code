package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactory;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactoryImpl;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.protocols.mdc.channels.VoidComChannel;

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

    private final IssueService issueService;
    private InboundComPortExecutorFactory inboundComPortExecutorFactory;

    public SingleThreadedComPortListener(InboundComPort comPort, ComServerDAO comServerDAO,
                                         ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor,
                                         InboundComPortExecutorFactory inboundComPortExecutorFactory, IssueService issueService) {
        super(comPort, comServerDAO, threadFactory, deviceCommandExecutor);
        this.inboundComPortExecutorFactory = inboundComPortExecutorFactory;
        this.issueService = issueService;
    }

    public SingleThreadedComPortListener(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, IssueService issueService) {
        this(comPort, comServerDAO, new PooledThreadFactory(), deviceCommandExecutor, new InboundComPortExecutorFactoryImpl(), issueService);
    }

    @Override
    protected void doRun() {
        ComChannel comChannel = listen();
        if (!(comChannel instanceof VoidComChannel)) {
            handleInboundDeviceProtocol(comChannel);
        }
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
    protected void handleInboundDeviceProtocol(ComChannel comChannel) {
        this.inboundComPortExecutorFactory.create(getServerInboundComPort(), getComServerDAO(), getDeviceCommandExecutor(), issueService).execute(comChannel);
    }
}