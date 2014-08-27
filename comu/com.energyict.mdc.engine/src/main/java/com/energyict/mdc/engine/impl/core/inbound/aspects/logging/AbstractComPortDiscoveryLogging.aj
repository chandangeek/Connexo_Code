package com.energyict.mdc.engine.impl.core.inbound.aspects.logging;

import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

import java.util.logging.Logger;

/**
 * Defines pointcuts and advice that will do logging for
 * inbound communication sessions.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (10:49)
 */
public abstract aspect AbstractComPortDiscoveryLogging {
    declare precedence:
            ComPortDiscoveryLogging,
            com.energyict.mdc.engine.impl.core.inbound.aspects.events.ComPortDiscoveryEventPublisher;

    private pointcut doDiscovery (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol):
           execution(InboundDeviceProtocol.DiscoverResultType doDiscovery(InboundDeviceProtocol))
        && target(handler)
        && args(inboundDeviceProtocol);

    before (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol): doDiscovery(handler, inboundDeviceProtocol) {
        ComPortDiscoveryLogger inboundComPortLogger = this.getUniqueLogger(handler);
        this.attachHandlerTo(inboundComPortLogger, handler.getContext());
        inboundComPortLogger.discoveryStarted(inboundDeviceProtocol.getClass().getName(), handler.getComPort());
    }

    after (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol)
    returning (InboundDeviceProtocol.DiscoverResultType resultType) : doDiscovery(handler, inboundDeviceProtocol) {
        ComPortDiscoveryLogger logger = this.getLogger(handler);
        switch (resultType) {
            case IDENTIFIER: {
                logger.discoveryFoundIdentifierOnly(inboundDeviceProtocol.getDeviceIdentifier(), handler.getComPort());
                break;
            }
            case DATA: {
                logger.discoveryFoundIdentifierAndData(inboundDeviceProtocol.getDeviceIdentifier(), handler.getComPort());
                break;
            }
            default: {
                throw new CommunicationException(MessageSeeds.UNSUPPORTED_DISCOVERY_RESULT_TYPE, resultType);
            }
        }
    }

    private pointcut provideResponse (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType responseType):
           execution(void provideResponse(InboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType))
        && target(handler)
        && args(inboundDeviceProtocol, responseType);

    before (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType responseType):
        provideResponse(handler, inboundDeviceProtocol, responseType)
    {
        ComPortDiscoveryLogger logger = this.getLogger(handler);
        switch (responseType) {
            case DEVICE_NOT_FOUND: {
                logger.deviceNotFound(inboundDeviceProtocol.getDeviceIdentifier(), handler.getComPort());
                break;
            }
            case FAILURE: {
                logger.discoveryFailed(inboundDeviceProtocol.getClass().getName(), handler.getComPort());
                break;
            }
            case DEVICE_DOES_NOT_EXPECT_INBOUND: {
                logger.deviceNotConfiguredForInboundCommunication(inboundDeviceProtocol.getDeviceIdentifier(), handler.getComPort());
                break;
            }
            case ENCRYPTION_REQUIRED: {
                logger.deviceRequiresEncryptedData(inboundDeviceProtocol.getDeviceIdentifier(), handler.getComPort());
                break;
            }
            case SERVER_BUSY: {
                logger.serverTooBusy(inboundDeviceProtocol.getDeviceIdentifier(), handler.getComPort());
                break;
            }
            case SUCCESS: {
                logger.deviceIdentified(inboundDeviceProtocol.getDeviceIdentifier(), handler.getComPort());
                break;
            }
            default: {
            }
        }
    }

    protected abstract ComPortDiscoveryLogger getUniqueLogger (InboundCommunicationHandler handler);

    protected abstract Logger attachHandlerTo (ComPortDiscoveryLogger loggger, InboundDiscoveryContextImpl context);

    protected abstract ComPortDiscoveryLogger getLogger (InboundCommunicationHandler handler);

    protected LogLevel getServerLogLevel (InboundCommunicationHandler handler) {
        return this.getServerLogLevel(handler.getComPort());
    }

    private LogLevel getServerLogLevel (ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.map(comServer.getServerLogLevel());
    }

}