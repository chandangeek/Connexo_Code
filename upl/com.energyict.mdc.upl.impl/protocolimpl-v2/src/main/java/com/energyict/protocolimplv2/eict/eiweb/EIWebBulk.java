package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.inbound.ServletBasedInboundDeviceProtocol;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final String MAX_IDLE_TIME = "maxIdleTime";
    private static final BigDecimal MAX_IDLE_TIME_DEFAULT_VALUE = BigDecimal.valueOf(200000);

    private HttpServletRequest request;
    private HttpServletResponse response;
    private InboundDiscoveryContext context;
    private ProtocolHandler protocolHandler;
    private ResponseWriter responseWriter;

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
        this.context.setCryptographer(new EIWebCryptographer(context.getInboundDAO(), context.getComPort()));
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return this.context;
    }

    @Override
    public void init(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }


    @Override
    public String getVersion() {
        return "$Date: 2016-05-31 16:24:54 +0300 (Tue, 31 May 2016)$";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<>(0);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        PropertySpec maxIdleTimePropertySpec = PropertySpecFactory.bigDecimalPropertySpec(MAX_IDLE_TIME, MAX_IDLE_TIME_DEFAULT_VALUE);
        return Arrays.asList(maxIdleTimePropertySpec);
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // Properties are not used in this class.
        // Note that the maxIdleTime property is used while setting up the Jetty servlet.
    }

    @Override
    public DiscoverResultType doDiscovery() {
        this.response.setContentType("text/html");
        try {
            this.responseWriter = new ResponseWriter(this.response);
            this.protocolHandler = new ProtocolHandler(this.responseWriter, this.context.getInboundDAO(), this.context.getCryptographer());
            try {
                this.protocolHandler.handle(this.request, this.context.getLogger());
            } catch (RuntimeException e) {
                this.responseWriter.failure();
                throw e;
            }
        } catch (IOException e) {
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
        return DiscoverResultType.DATA;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (!this.response.isCommitted()) {
            switch (responseType) {
                case SUCCESS:
                    this.responseWriter.success();
                    break;
                case DATA_ONLY_PARTIALLY_HANDLED:
                case FAILURE:
                case STORING_FAILURE:
                case DEVICE_DOES_NOT_EXPECT_INBOUND:
                case ENCRYPTION_REQUIRED:
                case DEVICE_NOT_FOUND:
                case DUPLICATE_DEVICE:
                case SERVER_BUSY:
                    this.responseWriter.failure();
                    break;
            }
        }
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.protocolHandler.getDeviceIdentifier();
    }

    @Override
    public String getAdditionalInformation() {
        return protocolHandler.getAdditionalInfo();
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return this.protocolHandler.getCollectedData();
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

}