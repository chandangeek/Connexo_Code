package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLInboundDeviceProtocolAdapter;
import com.energyict.mdc.upl.BinaryInboundDeviceProtocol;
import com.energyict.mdc.upl.Services;

import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 22/10/12
 * Time: 11:14
 */
public class InboundComPortExecutorImpl implements InboundComPortExecutor {

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

    private InboundDiscoveryContextImpl newInboundDiscoveryContext(ComPortRelatedComChannel comChannel) {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(comPort, comChannel, this.serviceProvider.connectionTaskService());
        // Todo: needs revision as soon as we get more experience with inbound protocols that need encryption
        context.setLogger(Logger.getAnonymousLogger());
        context.setComServerDAO(comServerDAO);

        context.setObjectMapperService(Services.objectMapperService());
        context.setCollectedDataFactory(Services.collectedDataFactory());
        context.setIssueFactory(Services.issueFactory());
        context.setPropertySpecService(Services.propertySpecService());
        context.setNlsService(Services.nlsService());
        context.setConverter(Services.converter());
        context.setDeviceGroupExtractor(Services.deviceGroupExtractor());
        context.setDeviceExtractor(Services.deviceExtractor());
        context.setDeviceMasterDataExtractor(Services.deviceMasterDataExtractor());
        context.setMessageFileExtractor(Services.deviceMessageFileExtractor());
        context.setX509Service(Services.x509Service());
        context.setKeyStoreService(Services.keyStoreService());
        context.setCertificateWrapperExtractor(Services.certificateWrapperExtractor());
        context.setCertificateAliasFinder(Services.certificateAliasFinder());

        return context;
    }

    private BinaryInboundDeviceProtocol newInboundDeviceProtocol() {
        com.energyict.mdc.upl.InboundDeviceProtocol inboundDeviceProtocol = this.comPort.getComPortPool().getDiscoveryProtocolPluggableClass().getInboundDeviceProtocol();
        if (inboundDeviceProtocol instanceof UPLInboundDeviceProtocolAdapter) {
            inboundDeviceProtocol = ((UPLInboundDeviceProtocolAdapter) inboundDeviceProtocol).getUplInboundDeviceProtocol();
        }
        return (BinaryInboundDeviceProtocol) inboundDeviceProtocol;
    }

    public interface ServiceProvider extends InboundCommunicationHandler.ServiceProvider {
    }
}