package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import java.util.logging.Logger;

/**
 *
 * Copyrights EnergyICT
 * Date: 22/10/12
 * Time: 11:14
 */
public class InboundComPortExecutorImpl implements InboundComPortExecutor {

    private final InboundComPort comPort;
    private final ComServerDAO comServerDAO;
    private final DeviceCommandExecutor deviceCommandExecutor;
    private final IssueService issueService;

    public InboundComPortExecutorImpl(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, IssueService issueService) {
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.issueService = issueService;
    }

    @Override
    public void execute(ComChannel comChannel) {
        final InboundCommunicationHandler inboundCommunicationHandler = new InboundCommunicationHandler(getServerInboundComPort(), this.comServerDAO, this.deviceCommandExecutor, issueService);
        BinaryInboundDeviceProtocol inboundDeviceProtocol = this.newInboundDeviceProtocol();
        InboundDiscoveryContextImpl context = this.newInboundDiscoveryContext(comChannel);
        inboundDeviceProtocol.initializeDiscoveryContext(context);
        inboundDeviceProtocol.initComChannel(comChannel);
        inboundCommunicationHandler.handle(inboundDeviceProtocol, context);
    }

    private InboundComPort getServerInboundComPort() {
        return this.comPort;
    }

    private InboundDiscoveryContextImpl newInboundDiscoveryContext (ComChannel comChannel) {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(comPort, comChannel);
        // Todo: needs revision as soon as we get more experience with inbound protocols that need encryption
        context.setLogger(Logger.getAnonymousLogger());
        return context;
    }

    private BinaryInboundDeviceProtocol newInboundDeviceProtocol() {
        return (BinaryInboundDeviceProtocol) this.comPort.getComPortPool().getDiscoveryProtocolPluggableClass().getInboundDeviceProtocol();
    }

}
