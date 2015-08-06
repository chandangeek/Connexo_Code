package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

/**
 * Copyrights EnergyICT
 * Date: 05.08.15
 * Time: 15:03
 */
public class ProvideInboundResponseDeviceCommandImpl  extends DeviceCommandImpl implements ProvideInboundResponseDeviceCommand {

    private final InboundCommunicationHandler inboundCommunicationHandler;
    private final InboundDeviceProtocol inboundDeviceProtocol;
    private boolean success = true; // optimistic

    public ProvideInboundResponseDeviceCommandImpl(ServiceProvider serviceProvider, InboundCommunicationHandler inboundCommunicationHandler, InboundDeviceProtocol inboundDeviceProtocol) {
        super(serviceProvider);
        this.inboundCommunicationHandler = inboundCommunicationHandler;
        this.inboundDeviceProtocol = inboundDeviceProtocol;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        try {
//            inboundCommunicationHandler.provideResponse(inboundDeviceProtocol, success ? InboundDeviceProtocol.DiscoverResponseType.SUCCESS : InboundDeviceProtocol.DiscoverResponseType.STORING_FAILURE);
        } catch (Exception e) {
            //TODO update the comsession
            e.printStackTrace();
        }
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("response to inbound device").append(success);
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Provide response to inbound device";
    }

    @Override
    public void dataStorageFailed() {
        this.success = false;
    }
}
