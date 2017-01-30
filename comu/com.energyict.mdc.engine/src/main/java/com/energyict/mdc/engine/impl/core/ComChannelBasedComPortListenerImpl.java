package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;
import com.energyict.mdc.io.InboundCommunicationException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.io.SocketService;

import java.util.concurrent.ThreadFactory;

/**
 * Models ComPortListener functionality specifically for a {@link ComChannel}
 * based {@link com.energyict.mdc.engine.config.ComPort ComPort}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 9:07
 */
public abstract class ComChannelBasedComPortListenerImpl extends ComPortListenerImpl {

    public interface ServiceProvider extends InboundComPortExecutorImpl.ServiceProvider {

        SocketService socketService();

        ComServerDAO comServerDAO();

        ThreadFactory threadFactory();

        InboundComPortConnectorFactory inboundComPortConnectorFactory();

    }

    private final InboundComPortConnector inboundComPortConnector;

    ComChannelBasedComPortListenerImpl(RunningComServer runningComServer, InboundComPort comPort, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, serviceProvider.clock(), serviceProvider.comServerDAO(), serviceProvider.threadFactory(), deviceCommandExecutor, serviceProvider);
        this.inboundComPortConnector = serviceProvider.inboundComPortConnectorFactory().connectorFor(comPort);
    }

    private InboundComPortConnector getInboundComPortConnector() {
        return this.inboundComPortConnector;
    }

    ComPortRelatedComChannel listen() {
        this.getLogger().listening(this.getThreadName());
        ComPortRelatedComChannel comChannel = getInboundComPortConnector().accept();
        this.registerActivity();
        comChannel.setComPort(this.getComPort());
        return comChannel;
    }

    @Override
    protected void doShutdown() {
        if (this.inboundComPortConnector != null) {
            try {
                this.inboundComPortConnector.close();
            }
            catch (Exception e) {
                throw new InboundCommunicationException(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION, e);
            }
        }
        super.doShutdown();
    }

}