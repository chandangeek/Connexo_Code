package com.energyict.protocolimplv2.eict.eiweb;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.inbound.ServletBasedInboundDeviceProtocol;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Clock;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ServletBasedInboundDeviceProtocol} interface
 * that will support devices that do inbound communication using the "EIWebBulk" protocol.
 * Large parts of the class were copied from the <code>com.energyict.eiwebbulk.EIWebBulk</code> class.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2012-10-12 (10:27:03)
 */
public class EIWebBulk implements ServletBasedInboundDeviceProtocol {

    private final Clock clock;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private InboundDiscoveryContext context;
    private ProtocolHandler protocolHandler;
    private ResponseWriter responseWriter;

    @Inject
    public EIWebBulk(Clock clock) {
        super();
        this.clock = clock;
    }

    @Override
    public void initializeDiscoveryContext (InboundDiscoveryContext context) {
        this.context = context;
        this.context.setCryptographer(new EIWebCryptographer(context));
    }

    @Override
    public InboundDiscoveryContext getContext () {
        return this.context;
    }

    @Override
    public void init (HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public String getVersion () {
        return "$Date: 2013-09-24 10:49:50 +0200 (Tue, 24 Sep 2013) $";
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        return null;
    }

    @Override
    public void copyProperties (TypedProperties properties) {
        // No pluggable properties so ignore this call
    }

    @Override
    public DiscoverResultType doDiscovery () {
        this.response.setContentType("text/html");
        try {
            this.responseWriter = new ResponseWriter(this.response);
            this.protocolHandler = new ProtocolHandler(this.responseWriter, this.context, this.context.getCryptographer(), clock);
            try {
                this.protocolHandler.handle(this.request, this.context.getLogger());
            }
            catch (RuntimeException e) {
                this.responseWriter.failure();
                throw e;
            }
        }
        catch (IOException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
        return DiscoverResultType.DATA;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if(!this.response.isCommitted()){
            try {
                switch (responseType) {
                    case SUCCESS:
                        this.responseWriter.success();
                        break;
                    case DEVICE_DOES_NOT_EXPECT_INBOUND:
                        this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "The device is not configured for inbound communication, request refused!");
                        break;
                    case ENCRYPTION_REQUIRED:
                        this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "The device with the id specified in the request requires encrypted data to be sent.");
                        break;
                    case DEVICE_NOT_FOUND:
                        this.response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Device for which data is posted does not exist.");
                        break;
                    case SERVER_BUSY:
                        this.response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service is temporarily unavailable due to a high load on the data storage component.");
                        break;
                }
            } catch (IOException e) {
                throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            }
        }
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier () {
        return this.protocolHandler.getDeviceIdentifier();
    }

    @Override
    public List<CollectedData> getCollectedData () {
        return this.protocolHandler.getCollectedData();
    }

}