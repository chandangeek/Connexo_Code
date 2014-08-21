package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.inbound.BinaryInboundDeviceProtocol;

import java.util.logging.Logger;

/**
 *
 * Copyrights EnergyICT
 * Date: 22/10/12
 * Time: 11:14
 */
public class InboundComPortExecutorImpl implements InboundComPortExecutor {

    public interface ServiceProvider extends InboundCommunicationHandler.ServiceProvider {
    }

    private final InboundComPort comPort;
    private final ComServerDAO comServerDAO;
    private final DeviceCommandExecutor deviceCommandExecutor;
    private final ServiceProvider serviceProvider;

    public InboundComPortExecutorImpl(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void execute(ComPortRelatedComChannel comChannel) {
        final InboundCommunicationHandler inboundCommunicationHandler = new InboundCommunicationHandler(getServerInboundComPort(), this.comServerDAO, this.deviceCommandExecutor, this.serviceProvider);
        BinaryInboundDeviceProtocol inboundDeviceProtocol = this.newInboundDeviceProtocol();
        InboundDiscoveryContextImpl context = this.newInboundDiscoveryContext(comChannel);
        inboundDeviceProtocol.initializeDiscoveryContext(context);
        inboundDeviceProtocol.initComChannel(comChannel);
        inboundCommunicationHandler.handle(inboundDeviceProtocol, context);
    }

    private InboundComPort getServerInboundComPort() {
        return this.comPort;
    }

    private InboundDiscoveryContextImpl newInboundDiscoveryContext (ComPortRelatedComChannel comChannel) {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(comPort, comChannel, this.serviceProvider.deviceDataService());
        // Todo: needs revision as soon as we get more experience with inbound protocols that need encryption
        context.setLogger(Logger.getAnonymousLogger());
        return context;
    }

    private BinaryInboundDeviceProtocol newInboundDeviceProtocol() {
        return (BinaryInboundDeviceProtocol) this.comPort.getComPortPool().getDiscoveryProtocolPluggableClass().getInboundDeviceProtocol();
    }

}
