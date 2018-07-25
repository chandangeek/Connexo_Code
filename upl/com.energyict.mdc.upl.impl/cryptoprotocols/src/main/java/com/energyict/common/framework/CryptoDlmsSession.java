package com.energyict.common.framework;

import com.energyict.dlms.protocolimplv2.ApplicationServiceObjectV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.protocol.ComChannel;

/**
 * V2 version of the DlmsSession object, using a comChannel instead of input/output streams.
 * For usage with DeviceProtocols (not MeterProtocol or SmartMeterProtocol)
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 25/02/2015 - 14:56
 */
public class CryptoDlmsSession extends DlmsSession {

    public CryptoDlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        super(comChannel, properties);
    }

    @Override
    protected ApplicationServiceObjectV2 buildAso(String calledSystemTitleString) {
        return new CryptoApplicationServiceObjectV2(
                buildXDlmsAse(),
                this,
                buildSecurityContext(),
                getContextId(),
                calledSystemTitleString == null ? null : calledSystemTitleString.getBytes(),
                null,
                null);  //CallingAEQualifier not yet supported here, no suite1/2 ECDSA available yet in HSM
    }

    /**
     * Build a new {@link CryptoSecurityContext}, using the {@link DlmsSessionProperties}
     */
    @Override
    protected CryptoSecurityContext buildSecurityContext() {
        return new CryptoSecurityContext(
                getProperties().getDataTransportSecurityLevel(),
                getProperties().getAuthenticationSecurityLevel(),
                getProperties().getSecuritySuite(),
                (getProperties().getSystemIdentifier() == null) ? null : getProperties().getSystemIdentifier(),
                getProperties().getSecurityProvider(),
                getProperties().getCipheringType().getType(),
                getProperties().getGeneralCipheringKeyType(),
                getProperties().incrementFrameCounterForReplyToHLS()
        );
    }
}