package com.energyict.common.framework;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.aso.AuthenticationTypes;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.protocolimplv2.ApplicationServiceObjectV2;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exceptions.HsmException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Extension of the 'normal' {@link ApplicationServiceObjectV2}, replacing every manual security operation with an HSM call.
 * <p/>
 * Copyrights EnergyICT
 * Author: khe
 */
public class CryptoApplicationServiceObjectV2 extends ApplicationServiceObjectV2 {

    public CryptoApplicationServiceObjectV2(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext, int contextId, byte[] calledAPTitle, byte[] calledAEQualifier, byte[] callingAEQualifier) {
        super(xDlmsAse, protocolLink, securityContext, contextId, calledAPTitle, calledAEQualifier, callingAEQualifier);
    }

    @Override
    protected void initACSE(SecurityContext securityContext, int contextId) {
        this.acse = new CryptoAssociationControlServiceElement(this.xDlmsAse, contextId, securityContext);
    }

    @Override
    protected void handleHighLevelSecurityAuthentication() throws UnsupportedException {
        //HLS3, 4, 5, 6 and 7 require a StoC (Server to Client challenge)
        if (this.acse.getRespondingAuthenticationValue() == null && (this.securityContext.getAuthenticationType().getLevel() > 2)) {
            silentDisconnect();
            ConnectionException connectionException = new ConnectionException("No challenge was responded; Current authenticationLevel(" + this.securityContext.getAuthenticationLevel() +
                    ") requires the server to respond with a challenge.");
            throw CommunicationException.protocolConnectFailed(connectionException);
        }

        //Need to check the digest in case of HLS3/4/5
        if (this.securityContext.getAuthenticationLevel() > 2) {

            byte[] clientDigest = calculateDigest(this.acse.getRespondingAuthenticationValue(), this.securityContext.getInitializationVector());
            byte[] response = replyToHLSAuthentication(clientDigest);

            byte[] iv = null;
            if (this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS5_GMAC) {
                byte[] fc = ProtocolUtils.getSubArray2(response, 1, 4);
                iv = ProtocolUtils.concatByteArrays(this.securityContext.getResponseSystemTitle(), fc);
            }

            byte[] calculatedServerDigest = calculateDigest(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), iv);

            if (!Arrays.equals(calculatedServerDigest, response)) {
                silentDisconnect();
                IOException ioException = new IOException("HighLevelAuthentication failed, client and server challenges do not match.");
                throw CommunicationException.protocolConnectFailed(ioException);
            }
        }

        this.associationStatus = ASSOCIATION_CONNECTED;
    }

    /**
     * Calculate the digest of the given challenge, based on the configured security properties and the Authentication Access Level.
     */
    private byte[] calculateDigest(byte[] challenge, byte[] initialVector) {
        try {
            switch (this.securityContext.getAuthenticationType()) {
                case LOWEST_LEVEL:
                case LOW_LEVEL:
                    return new byte[0];
                case MAN_SPECIFIC_LEVEL:
                    throw DeviceConfigurationException.unsupportedPropertyValueWithReason("AuthenticationAccessLevel", String.valueOf(this.securityContext.getAuthenticationLevel()), "Level 2 (manufacturer specific) is not supported in this protocol.");
                case HLS3_MD5: {
                    IrreversibleKey hlsSecret = IrreversibleKeyImpl.fromByteArray(this.securityContext.getSecurityProvider().getHLSSecret());
                    return Services.hsmService().generateDigestMD5(challenge, hlsSecret);
                }
                case HLS4_SHA1: {
                    IrreversibleKey hlsSecret = IrreversibleKeyImpl.fromByteArray(this.securityContext.getSecurityProvider().getHLSSecret());
                    return Services.hsmService().generateDigestSHA1(challenge, hlsSecret);
                }
                case HLS5_GMAC: {
                    IrreversibleKey ak = IrreversibleKeyImpl.fromByteArray(this.securityContext.getSecurityProvider().getAuthenticationKey());
                    IrreversibleKey ek = IrreversibleKeyImpl.fromByteArray(this.securityContext.getSecurityProvider().getGlobalKey());
                    //TODO: use generateDigestGMAC when HSM will have the posibility to specify the security suite
                    //                    return Services.hsmService().generateDigestGMAC(challenge, initialVector, ak, ek, getSecurityContext().getSecuritySuite());
                    return Services.hsmService().authenticateApdu(challenge, initialVector, ak, ek, getSecurityContext().getSecuritySuite());
                }
                case HLS6_SHA256: {
                    throw DeviceConfigurationException.unsupportedPropertyValueWithReason("AuthenticationAccessLevel", String.valueOf(this.securityContext.getAuthenticationLevel()), "Level 6 (SHA256) is not yet supported by the HSM.");
                }
                case HLS7_ECDSA: {
                    throw DeviceConfigurationException.unsupportedPropertyValueWithReason("AuthenticationAccessLevel", String.valueOf(this.securityContext.getAuthenticationLevel()), "Level 7 (ECDSA) is not yet supported by the HSM.");
                }
                default: {
                    // should never get here
                    throw DeviceConfigurationException.unsupportedPropertyValue("AuthenticationAccessLevel", String.valueOf(this.securityContext.getAuthenticationLevel()));
                }
            }
        } catch (HsmException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
        }
    }
}