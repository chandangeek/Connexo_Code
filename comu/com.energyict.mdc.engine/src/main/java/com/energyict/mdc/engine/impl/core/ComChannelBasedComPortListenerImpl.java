package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;

import java.util.concurrent.ThreadFactory;

/**
 * Models ComPortListener functionality specifically for a {@link ComChannel}
 * based {@link com.energyict.mdc.engine.model.ComPort ComPort}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 9:07
 */
public abstract class ComChannelBasedComPortListenerImpl extends ComPortListenerImpl {

    private final InboundComPortConnector inboundComPortConnector;

    protected ComChannelBasedComPortListenerImpl(InboundComPort comPort, ComServerDAO comServerDAO, InboundComPortConnectorFactory inboundComPortConnectorFactory, ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor) {
        super(comPort, comServerDAO, threadFactory, deviceCommandExecutor);
        this.inboundComPortConnector = inboundComPortConnectorFactory.connectorFor(comPort);
    }

    protected InboundComPortConnector getInboundComPortConnector() {
        return this.inboundComPortConnector;
    }

    protected ComPortRelatedComChannel listen() {
        ComPortRelatedComChannel comChannel = getInboundComPortConnector().accept();
        comChannel.setComPort(this.getComPort());
        return comChannel;
    }

}