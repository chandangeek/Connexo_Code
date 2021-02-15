package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.common.framework.CryptoDlmsSession;
import com.energyict.common.tls.TLSHSMConnectionType;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.crypto.HsmProtocolService;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.Beacon3100Messaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.CryptoBeaconMessaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/10/2016 - 17:01
 */
public class CryptoBeacon3100 extends Beacon3100 {

    private final HsmProtocolService hsmProtocolService;

    public CryptoBeacon3100(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, ObjectMapperService objectMapperService, DeviceMasterDataExtractor extractor, DeviceGroupExtractor deviceGroupExtractor, CertificateWrapperExtractor certificateWrapperExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor, DeviceExtractor deviceExtractor, DeviceMessageFileExtractor deviceMessageFileExtractor, HsmProtocolService hsmProtocolService) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, objectMapperService, extractor, deviceGroupExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor, deviceExtractor, deviceMessageFileExtractor);
        this.hsmProtocolService = hsmProtocolService;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster EnergyICT Beacon3100 G3 DLMS crypto-protocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-01-26 $" + "/" + super.getVersion();
    }

    @Override
    public Beacon3100Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new CryptoBeacon3100Properties(getCertificateWrapperExtractor());
        }
        return (CryptoBeacon3100Properties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new CryptoBeacon3100ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    protected Beacon3100Messaging getBeacon3100Messaging() {
        if (beacon3100Messaging == null) {
            beacon3100Messaging = new CryptoBeaconMessaging(this, getCollectedDataFactory(), getIssueFactory(), getObjectMapperService(), getPropertySpecService(), getNlsService(), getConverter(), getExtractor(), getDeviceGroupExtractor(), getDeviceExtractor(), getCertificateWrapperExtractor(), getKeyAccessorTypeExtractor(), getDeviceMessageFileExtractor(), getHsmProtocolService());
        }
        return beacon3100Messaging;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> supportedConnectionTypes = new ArrayList<>(super.getSupportedConnectionTypes());
        supportedConnectionTypes.add(new TLSHSMConnectionType(getPropertySpecService(), getCertificateWrapperExtractor()));
        return supportedConnectionTypes;
    }

    @Override
    protected void initDlmsSession(ComChannel comChannel) {
        setDlmsSession(getCryptoDlmsSession(comChannel));
    }

    @Override
    protected DlmsSession getDlmsSessionForFCTesting(ComChannel comChannel) {
        return getCryptoDlmsSession(comChannel);
    }

    private DlmsSession getCryptoDlmsSession(ComChannel comChannel) {
        //Uses the HSM to encrypt requests and decrypt responses, we don't have the plain keys
        return new CryptoDlmsSession(comChannel, getDlmsSessionProperties());

    }

    @Override
    protected void readFrameCounter(ComChannel comChannel) {
        byte[] authenticationKeyBytes = getDlmsSessionProperties().getSecurityProvider().getAuthenticationKey();
        final IrreversibleKey authKey = IrreversibleKeyImpl.fromByteArray(authenticationKeyBytes);

        // construct a temporary session with 0:0 security and clientId=16 (public)
        final TypedProperties publicProperties = TypedProperties.copyOf(getDlmsSessionProperties().getProperties());
        publicProperties.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(ClientConfiguration.PUBLIC.clientId));
        final Beacon3100Properties publicClientProperties = new Beacon3100Properties(getCertificateWrapperExtractor());
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(ClientConfiguration.PUBLIC.clientId), 0, 0, 0, 0, 0, publicProperties));    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsSessionProperties().getSerialNumber());
        final ObisCode frameCounterObisCode = this.getFrameCounterObisCode(getDlmsSessionProperties().getClientMacAddress());
        final long frameCounter;

        if (getDlmsSessionProperties().isPublicClientPreEstablished() && getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()) {
            if (getLogger().isLoggable(Level.WARNING)) {
                getLogger().log(Level.WARNING, "Invalid configuration detected: cannot use a pre-established public client association in combination with and authenticated frame counter, overriding to non-pre-established.");
            }
        }
        final boolean preEstablished = getDlmsSessionProperties().isPublicClientPreEstablished() && !getDlmsSessionProperties().getRequestAuthenticatedFrameCounter();

        try {
            // Associate if necessary.
            if (preEstablished) {
                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger().log(Level.FINE, "Public client association is pre-established.");
                }

                publicDlmsSession.assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());
            } else {
                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger().log(Level.FINE, "Public client association is not pre-established.");
                }

                publicDlmsSession.getDlmsV2Connection().connectMAC();
                publicDlmsSession.createAssociation();
            }

            // Then read out the frame counter.
            if (getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()) {
                getLogger().info("Reading frame counter using secure method");
                try {
                    FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
                    frameCounterProvider.setSkipValidation(true);

                    frameCounter = frameCounterProvider.getFrameCounterHSM(authKey);
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, e.getCause() + e.getMessage(), e);
                    throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, e.getCause() + e.getMessage(), e);
                    final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame counter, cannot continue! " + e.getMessage());
                    throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
                }
            } else {
                try {
                    frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, e.getCause() + e.getMessage(), e);
                    throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
                }
            }
            getLogger().info("The read-out frame counter is: " + frameCounter);

        } catch (Exception e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the secure frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } finally {
            // Only disconnect if the association is not pre-established.
            if (!preEstablished) {
                publicDlmsSession.disconnect();
            }
        }

        // Set TX frame counter
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    protected HsmProtocolService getHsmProtocolService() {
        return hsmProtocolService;
    }
}