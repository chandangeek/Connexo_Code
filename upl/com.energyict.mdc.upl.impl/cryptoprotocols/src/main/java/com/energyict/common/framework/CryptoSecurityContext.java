package com.energyict.common.framework;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.XdlmsApduTags;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.encryption.asymetric.ECCCurve;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.crypto.DataAndAuthenticationTag;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exception.HsmException;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Extension of the 'normal' {@link SecurityContext}, replacing every manual security operation (encryption, authentication) with an HSM call.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/10/2016 - 11:57
 */
public class CryptoSecurityContext extends SecurityContext {

    private static final int SECURITY_SUITE_1 = 1;

    public CryptoSecurityContext(int dataTransportSecurityLevel,
                                 int associationAuthenticationLevel,
                                 int dataTransportEncryptionType,
                                 byte[] systemIdentifier,
                                 SecurityProvider securityProvider,
                                 int cipheringType,
                                 GeneralCipheringKeyType generalCipheringKeyType,
                                 final boolean incrementFrameCounterWhenReplyingToHLS) {
        super(dataTransportSecurityLevel, associationAuthenticationLevel, dataTransportEncryptionType, systemIdentifier, securityProvider, cipheringType, generalCipheringKeyType, incrementFrameCounterWhenReplyingToHLS);
    }

    /**
     * Encryption & authentication tag using the HSM.
     */
    @Override
    public byte[] dataTransportEncryption(byte[] plainText) throws UnsupportedException {

        try {
            byte[] encryptedRequest = null;
            byte[] tag = null;
            IrreversibleKey ak = IrreversibleKeyImpl.fromByteArray(getSecurityProvider().getAuthenticationKey());
            IrreversibleKey ek = IrreversibleKeyImpl.fromByteArray(getSecurityProvider().getGlobalKey());
            if (getSecurityPolicy().isRequestAuthenticatedOnly()) {
                tag = Services.hsmService().authenticateApdu(plainText, getInitializationVector(), ak, ek, getSecuritySuite());
            } else if (getSecurityPolicy().isRequestEncryptedOnly()) {
                encryptedRequest = Services.hsmService().encryptApdu(plainText, getInitializationVector(), ak, ek, getSecuritySuite());
            } else if (getSecurityPolicy().isRequestAuthenticatedAndEncrypted()) {
                DataAndAuthenticationTag dataAndAuthenticationTag = Services.hsmService().authenticateEncryptApdu(plainText, getInitializationVector(), ak, ek, getSecuritySuite());
                encryptedRequest = dataAndAuthenticationTag.getData();
                tag = dataAndAuthenticationTag.getAuthenticationTag();
            } else {
                throw new UnsupportedException("Unknown securityPolicy: " + this.getSecurityPolicy().getDataTransportSecurityLevel());
            }
            return createSecuredApdu(encryptedRequest == null ? plainText : encryptedRequest, tag);
        } catch (HsmException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
        } finally {
            incFrameCounter();
        }
    }

    @Override
    public byte[] dataTransportDecryption(byte[] cipherFrame, GeneralCipheringKeyType generalCipheringKeyType, byte[] generalCipheringHeader) throws ProtocolException, ConnectionException, DLMSConnectionException {

        int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
        int responseSecurityControlByte = (cipherFrame[LENGTH_INDEX + lengthOffset]) & 0xFF;

        //Check if the security of the response (indicated by its security control byte) is at least the same as the configured security level.
        boolean authenticatedResponse = ProtocolTools.isBitSet(responseSecurityControlByte, 4);
        boolean encryptedResponse = ProtocolTools.isBitSet(responseSecurityControlByte, 5);
        boolean shouldBeAuthenticated = getSecurityPolicy().isResponseAuthenticatedOnly() || getSecurityPolicy().isResponseAuthenticatedAndEncrypted();
        boolean shouldBeEncrypted = getSecurityPolicy().isResponseEncryptedOnly() || getSecurityPolicy().isResponseAuthenticatedAndEncrypted();

        if (!authenticatedResponse && shouldBeAuthenticated) {
            throw new ProtocolException("Received a response that has no authentication tag, while the configured security level states that all responses must be authenticated. Aborting.");
        }
        if (!encryptedResponse && shouldBeEncrypted) {
            throw new ProtocolException("Received an unencrypted response, while the configured security level states that all responses must be encrypted. Aborting.");
        }

        IrreversibleKey ak = IrreversibleKeyImpl.fromByteArray(getSecurityProvider().getAuthenticationKey());
        IrreversibleKey ek = IrreversibleKeyImpl.fromByteArray(getSecurityProvider().getGlobalKey());
        try {
            if (!authenticatedResponse && !encryptedResponse) {
                if (XdlmsApduTags.isGlobalCipheringTag(cipherFrame[0]) || XdlmsApduTags.isDedicatedCipheringTag(cipherFrame[0])) {
                    //An unsecured global-ciphering or dedicated-ciphering APDU. Unwrap it by stripping of the header.
                    cipherFrame = ProtocolTools.getSubArray(cipherFrame, 7);
                }

                return cipherFrame;
            } else if (authenticatedResponse && !encryptedResponse) {
                //Authentication only
                byte[] authTag = getAuthenticationTag(cipherFrame);
                byte[] apdu = getApdu(cipherFrame, true);
                byte[] iv = getRespondingInitializationVector();

                Services.hsmService().verifyApduAuthentication(apdu, authTag, iv, ak, ek, getSecuritySuite());
                return apdu;
            } else if (!authenticatedResponse) {
                //Encryption only
                byte[] cipheredAPDU = getApdu(cipherFrame, false);
                byte[] iv = getRespondingInitializationVector();

                return Services.hsmService().decryptApdu(cipheredAPDU, iv, ak, ek, getSecuritySuite());
            } else {
                //Both authentication and encryption
                byte[] cipheredAPDU = getApdu(cipherFrame, true);
                byte[] iv = getRespondingInitializationVector();
                byte[] authTag = getAuthenticationTag(cipherFrame);

                return Services.hsmService().verifyAuthenticationDecryptApdu(cipheredAPDU, authTag, iv, ak, ek, getSecuritySuite());
            }
        } catch (HsmException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
        }
    }

    @Override
    public byte[] applyGeneralSigning(byte[] securedRequest, ECCCurve eccCurve, byte[] dateTime, byte[] otherInfo, boolean includeRequestLength) throws UnsupportedException {
        byte[] generalCipheringHeader = createGeneralCipheringHeader(dateTime, otherInfo);
        byte[] requestData = includeRequestLength ? ProtocolTools.concatByteArrays(DLMSUtils.getAXDRLengthEncoding(securedRequest.length), securedRequest) : securedRequest;
        byte[] dataToSign = ProtocolTools.concatByteArrays(generalCipheringHeader, requestData);
        final String keyLabel = getGeneralCipheringSecurityProvider().getClientPrivateSigningKeyLabel();
        byte[] signature = new byte[0];
        try {
            signature = Services.hsmService().cosemGenerateSignature(SECURITY_SUITE_1, keyLabel, dataToSign);
        } catch (HsmException e) {
            throw new UnsupportedException("Unable to sign data using HSM! " + e.getMessage());
        }

        return ProtocolTools.concatByteArrays(
                generalCipheringHeader,
                DLMSUtils.getAXDRLengthEncoding(securedRequest.length),
                securedRequest,
                DLMSUtils.getAXDRLengthEncoding(signature.length),
                signature
        );
    }

    @Override
    public byte[] unwrapGeneralSigning(byte[] generalSigningAPDU) {
        throw DeviceConfigurationException.unsupportedPropertyValueWithReason("EncryptionAccessLevel", String.valueOf(getSecurityPolicy().getDataTransportSecurityLevel()), "Checking the signature of responses (ECDSA) is not yet supported");
    }

}