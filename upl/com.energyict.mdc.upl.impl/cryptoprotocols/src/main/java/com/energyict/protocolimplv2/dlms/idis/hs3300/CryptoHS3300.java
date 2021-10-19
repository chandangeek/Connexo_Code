package com.energyict.protocolimplv2.dlms.idis.hs3300;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.common.framework.CryptoDlmsSession;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.idis.hs3300.messages.CryptoHS3300Messaging;
import com.energyict.protocolimplv2.dlms.idis.hs3300.messages.HS3300Messaging;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300Properties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;

public class CryptoHS3300 extends HS3300 {

    public CryptoHS3300(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                        TariffCalendarExtractor calendarExtractor, NlsService nlsService, Converter converter,
                        DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                        KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, calendarExtractor, nlsService, converter,
              messageFileExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell HS3300 DLMS crypto-protocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-09-26$";
    }

    @Override
    public HS3300Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new CryptoHS3300Properties(getPropertySpecService(), getNlsService(), getCertificateWrapperExtractor());
        }
        return (CryptoHS3300Properties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new CryptoHS3300ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
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
    protected void readFrameCounterSecure(ComChannel comChannel) {
        getLogger().info(getLogPrefix()+"Reading frame counter using secure method (HSM validation)");

        byte[] authenticationKeyBytes = getDlmsSessionProperties().getSecurityProvider().getAuthenticationKey();
        final IrreversibleKey authKey = IrreversibleKeyImpl.fromByteArray(authenticationKeyBytes);

        // construct a temporary session with 0:0 security and clientId=16 (public)
        final TypedProperties publicProperties = TypedProperties.copyOf(getDlmsSessionProperties().getProperties());
        publicProperties.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
        final HS3300Properties publicClientProperties = new HS3300Properties(this.getPropertySpecService(), getNlsService(), getCertificateWrapperExtractor());
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT), 0, 0, 0, 0, 0, publicProperties));    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsSessionProperties().getSerialNumber());
        final ObisCode frameCounterObisCode = getFrameCounterForClient(getDlmsSessionProperties().getClientMacAddress());
        final long frameCounter;

        try {
            publicDlmsSession.getDlmsV2Connection().connectMAC();
            publicDlmsSession.createAssociation((int) getDlmsSessionProperties().getAARQTimeout());

            FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
            frameCounterProvider.setSkipValidation(getDlmsSessionProperties().skipFramecounterAuthenticationTag());

            frameCounter = frameCounterProvider.getFrameCounterHSM(authKey);

            getLogger().info(getLogPrefix()+"The read-out frame-counter is: " + frameCounter);

            publicDlmsSession.disconnect();

        } catch (IOException e) {
            getLogger().severe(getLogPrefix()+" I/O Exception while reading the secure frame-counter: "+e.getMessage());
            throw DLMSIOExceptionHandler.handleRecoverable(e, publicDlmsSession.getProperties().getRetries() + 1);
        } catch (Exception e) {
            getLogger().severe(getLogPrefix()+" Exception while reading the secure frame-counter: "+e.getMessage());
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the secure frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException); // this leaves the connection still intact
        }

        // Set TX frame counter
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    @Override
    protected HS3300Messaging getDeviceMessaging() {
        if (this.deviceMessaging == null) {
            this.deviceMessaging = new CryptoHS3300Messaging(this, getCollectedDataFactory(), getIssueFactory(),
                    getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(),
                    this.getCertificateWrapperExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return this.deviceMessaging;
    }

}
