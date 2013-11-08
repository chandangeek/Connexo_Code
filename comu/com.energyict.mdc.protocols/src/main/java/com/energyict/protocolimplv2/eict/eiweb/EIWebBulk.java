package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.inbound.ServletBasedInboundDeviceProtocol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
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

    private HttpServletRequest request;
    private HttpServletResponse response;
    private InboundDiscoveryContext context;
    private ProtocolHandler protocolHandler;
    private ResponseWriter responseWriter;

    @Override
    public void initializeDiscoveryContext (InboundDiscoveryContext context) {
        this.context = context;
        this.context.setCryptographer(new EIWebCryptographer(context.getInboundDAO(), context.getComPort()));
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
    public List<PropertySpec> getRequiredProperties () {
        return new ArrayList<>(0);
    }

    @Override
    public List<PropertySpec> getOptionalProperties () {
        return new ArrayList<>(0);
    }

    @Override
    public void addProperties (TypedProperties properties) {
        // No pluggable properties so ignore this call
    }

    @Override
    public DiscoverResultType doDiscovery () {
        this.response.setContentType("text/html");
        try {
            this.responseWriter = new ResponseWriter(this.response);
            this.protocolHandler = new ProtocolHandler(this.responseWriter, this.context.getInboundDAO(), this.context.getCryptographer());
            try {
                this.protocolHandler.handle(this.request, this.context.getLogger());
            }
            catch (RuntimeException e) {
                this.responseWriter.failure();
                throw e;
            }
        }
        catch (IOException e) {
            throw new CommunicationException(e);
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
                throw new CommunicationException(e);
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