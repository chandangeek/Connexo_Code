package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.tasks.history.ComSessionBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.logging.Logger;

/**
 * Holds contextual information for an {@link InboundDeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-15 (09:15)
 */
public class InboundDiscoveryContextImpl implements InboundDiscoveryContext {

    private Logger logger;
    private Cryptographer cryptographer;
    private InboundDAO inboundDAO;
    private ComSessionBuilder sessionBuilder = new ComSessionBuilder();
    private final InboundComPort comPort;
    private ComChannel comChannel;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;

    public InboundDiscoveryContextImpl(InboundComPort comPort, ComChannel comChannel) {
        super();
        this.comPort = comPort;
        this.comChannel = comChannel;
    }

    public InboundDiscoveryContextImpl(InboundComPort comPort, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        super();
        this.comPort = comPort;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Cryptographer getCryptographer() {
        return cryptographer;
    }

    @Override
    public void setCryptographer(Cryptographer cryptographer) {
        this.cryptographer = cryptographer;
    }

    public InboundDAO getInboundDAO () {
        return inboundDAO;
    }

    public void setInboundDAO (InboundDAO inboundDAO) {
        this.inboundDAO = inboundDAO;
    }

    public ComSessionBuilder getComSessionShadow () {
        return sessionBuilder;
    }

    public InboundComPort getComPort () {
        return comPort;
    }

    @Override
    public ComChannel getComChannel() {
        return comChannel;
    }

    @Override
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    @Override
    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    @Override
    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
        return this.getInboundDAO().confirmSentMessagesAndGetPending(deviceIdentifier, confirmationCount);
    }

    @Override
    public List<SecurityProperty> getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier) {
        return this.getInboundDAO().getDeviceProtocolSecurityProperties(deviceIdentifier, this.getComPort());
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
        return this.getInboundDAO().getDeviceConnectionTypeProperties(deviceIdentifier, this.getComPort());
    }

    @Override
    public TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier) {
        return this.getInboundDAO().getDeviceProtocolProperties(deviceIdentifier);
    }

}