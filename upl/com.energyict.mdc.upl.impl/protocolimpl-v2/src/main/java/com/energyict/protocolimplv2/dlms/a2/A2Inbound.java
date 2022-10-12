package com.energyict.protocolimplv2.dlms.a2;

import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.BinaryInboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDAO;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataEncryptionException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.a2.properties.A2ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.a2.properties.A2Properties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class A2Inbound implements BinaryInboundDeviceProtocol {

    protected ComChannel comChannel;
    private InboundDiscoveryContext context;
    private DlmsProperties dlmsProperties;
    private DeviceIdentifier deviceIdentifier;
    protected HasDynamicProperties dlmsConfigurationSupport;

    private final static int PUBLIC_CLIENT = 16;
    private final static ObisCode FRAME_COUNTER_MANAGEMENT = ObisCode.fromString("0.0.43.1.1.255");
    private final static ObisCode COSEM_LOGICAL_DEVICE_NAME = ObisCode.fromString("0.0.42.0.0.255");
    private DlmsSession dlmsSession;
    private InboundDAO inboundDAO;
    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;
    
    private PropertySpecService propertySpecService;

    public A2Inbound(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return context;
    }

    @Override
    public DiscoverResultType doDiscovery() {
        this.inboundDAO = context.getInboundDAO();
        readFrameCounter(comChannel);
        setDlmsSession(createDlmsSession(comChannel, getDlmsProperties()));
        return DiscoverResultType.IDENTIFIER;
    }

    private void readFrameCounter(ComChannel comChannel) {
        TypedProperties clone = getDlmsProperties().getProperties().clone();
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
        DlmsProperties publicClientProperties = new DlmsProperties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(
                new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT), 0, 0, 0, 0, 0, clone)
        );  // SecurityLevel 0:0

        long frameCounter;
        String logicalDeviceName;
        DlmsSession publicDlmsSession = getPublicDlmsSession(comChannel, publicClientProperties);
        try {
            getLogger().info("Public client connected, reading frame counter " + FRAME_COUNTER_MANAGEMENT.toString() + ", corresponding to client " + publicClientProperties.getClientMacAddress());
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(FRAME_COUNTER_MANAGEMENT).getValueAttr().longValue();
            getLogger().info("Frame counter received: " + frameCounter);

            getLogger().info("Reading COSEM logical device name " + COSEM_LOGICAL_DEVICE_NAME.toString() + ", corresponding to client " + publicClientProperties.getClientMacAddress());
            logicalDeviceName = publicDlmsSession.getCosemObjectFactory().getData(COSEM_LOGICAL_DEVICE_NAME).getValueAttr().getOctetString().stringValue();
            getLogger().info("COSEM logical device name received: " + logicalDeviceName);
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        } finally {
            getLogger().info("Disconnecting public client");
            publicDlmsSession.disconnect();
        }

        getDlmsProperties().setSerialNumber(logicalDeviceName);
        this.deviceIdentifier = new DeviceIdentifierBySerialNumber( logicalDeviceName );

        SecurityContext securityContext = getSecurityContext( this.deviceIdentifier );
        getDlmsProperties().setSecurityProvider( securityContext.getSecurityProvider() );
        getDlmsProperties().setSecurityPropertySet( getSecurityPropertySet( this.deviceIdentifier ) );

        getDlmsProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    protected DlmsSession getPublicDlmsSession(ComChannel comChannel, DlmsProperties dlmsProperties) {
        DlmsSession publicDlmsSession = createDlmsSession(comChannel, dlmsProperties);
        getLogger().info("Connecting to public client:" + PUBLIC_CLIENT);
        connectWithRetries(publicDlmsSession);
        return publicDlmsSession;
    }

    public A2DlmsSession createDlmsSession(ComChannel comChannel, DlmsProperties dlmsProperties) {
        return new A2DlmsSession(comChannel, dlmsProperties, getLogger());
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     *
     * @param dlmsSession DlmsSession to use
     */
    protected void connectWithRetries(DlmsSession dlmsSession) {
        int tries = 0;
        while (true) {
            ProtocolRuntimeException exception;
            try {
                dlmsSession.getDLMSConnection().setRetries(0); // Temporarily disable retries in the connection layer, AARQ retries are handled here
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.getDlmsV2Connection().connectMAC();
                    dlmsSession.createAssociation();
                }
                return;
            } catch (ProtocolRuntimeException e) {
                getLogger().log(Level.WARNING, e.getMessage(), e);
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;    // Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (e instanceof ConnectionCommunicationException) {
                    throw e;
                } else if (e instanceof DataEncryptionException) {
                    throw e;
                }
                exception = e;
            } finally {
               dlmsSession.getDLMSConnection().setRetries(getDlmsProperties().getRetries());
            }

            // Release and retry the AARQ in case of ACSE exception
            if (++tries > dlmsSession.getProperties().getRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries.");
                throw CommunicationException.protocolConnectFailed(exception);
            } else {
                getLogger().info("Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (ProtocolRuntimeException e) {
                    dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
            }
        }
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        // Nothing to do here - the device doesn't expect an answer from the head-end system
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public String getAdditionalInformation() {
        return "";
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return true;
    }

    @Override
    public String getVersion() {
        return "$Date: 2019-11-29 12:00:00 +0200 (Fri, 29 Sep 2019) $";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return getDlmsConfigurationSupport().getUPLPropertySpecs();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        getDlmsConfigurationSupport().setUPLProperties(properties);
    }

    public A2Properties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new A2Properties();
        }
        return (A2Properties) dlmsProperties;
    }

    protected SecurityContext getSecurityContext(DeviceIdentifier originDeviceIdentified) {
        DlmsProperties securityProperties = getNewInstanceOfProperties();
        securityProperties.setSecurityPropertySet(getSecurityPropertySet(originDeviceIdentified));
        securityProperties.addProperties(getSecurityPropertySet(originDeviceIdentified).getSecurityProperties());
        this.dlmsSession = createDlmsSession(comChannel, securityProperties);
        SecurityContext securityContext = dlmsSession.getAso().getSecurityContext();
        securityContext.getSecurityProvider().setRespondingFrameCounterHandling(new DefaultRespondingFrameCounterHandler());
        return securityContext;
    }

    public DeviceProtocolSecurityPropertySet getSecurityPropertySet(DeviceIdentifier anyDeviceIdentifier) {
        if (deviceProtocolSecurityPropertySet == null) {
            this.deviceProtocolSecurityPropertySet = getContext()
                    .getDeviceProtocolSecurityPropertySet(deviceIdentifier)
                    .orElseThrow(() -> CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier));
        }
        return this.deviceProtocolSecurityPropertySet;
    }

    /**
     * A collection of general DLMS properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new A2ConfigurationSupport(this.propertySpecService);
        }
        return dlmsConfigurationSupport;
    }

    protected DlmsProperties getNewInstanceOfProperties() {
        return new A2Properties();
    }

    public void setDlmsSession(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected Logger getLogger() {
        return getContext().getLogger();
    }

}
