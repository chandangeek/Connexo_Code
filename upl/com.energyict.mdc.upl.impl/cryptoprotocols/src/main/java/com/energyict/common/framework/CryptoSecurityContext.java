package com.energyict.common.framework;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.encryption.asymetric.ECCCurve;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.protocol.exception.DeviceConfigurationException;

/**
 * Extension of the 'normal' {@link SecurityContext}, replacing every manual security operation (encryption, authentication) with an HSM call.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/10/2016 - 11:57
 */
public class CryptoSecurityContext extends SecurityContext {

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

//        if (getSecurityPolicy().isRequestPlain()) {
//            return plainText;
//        } else {
//            try {
//                byte[] encryptedRequest = null;
//                byte[] tag = null;
//                IrreversibleKey ak = IrreversibleKey.fromByteArray(getSecurityProvider().getAuthenticationKey());
//                IrreversibleKey ek = IrreversibleKey.fromByteArray(getSecurityProvider().getGlobalKey());
//                if (getSecurityPolicy().isRequestAuthenticatedOnly()) {
//                    tag = ProtocolService.INSTANCE.get().authenticateApdu(plainText, getInitializationVector(), ak, ek);
//                } else if (getSecurityPolicy().isRequestEncryptedOnly()) {
//                    encryptedRequest = ProtocolService.INSTANCE.get().encryptApdu(plainText, getInitializationVector(), ak, ek);
//                } else if (getSecurityPolicy().isRequestAuthenticatedAndEncrypted()) {
//                    DataAndAuthenticationTag dataAndAuthenticationTag = ProtocolService.INSTANCE.get().authenticateEncryptApdu(plainText, getInitializationVector(), ak, ek);
//                    encryptedRequest = dataAndAuthenticationTag.getData();
//                    tag = dataAndAuthenticationTag.getAuthenticationTag();
//                } else {
//                    throw new UnsupportedException("Unknown securityPolicy: " + this.getSecurityPolicy().getDataTransportSecurityLevel());
//                }
//                return createSecuredApdu(encryptedRequest == null ? plainText : encryptedRequest, tag);
//            } catch (HsmException e) {
//                throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
//            } finally {
//                    incFrameCounter();
//            }
//        }
        //TODO: make available connexo hsmService
        return null;
    }

    @Override
    public byte[] dataTransportDecryption(byte[] cipherFrame, GeneralCipheringKeyType generalCipheringKeyType, byte[] generalCipheringHeader) throws ProtocolException, ConnectionException, DLMSConnectionException {

//        int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
//        int responseSecurityControlByte = (cipherFrame[LENGTH_INDEX + lengthOffset]) & 0xFF;
//
//        //Check if the security of the response (indicated by its security control byte) is at least the same as the configured security level.
//        boolean authenticatedResponse = ProtocolTools.isBitSet(responseSecurityControlByte, 4);
//        boolean encryptedResponse = ProtocolTools.isBitSet(responseSecurityControlByte, 5);
//        boolean shouldBeAuthenticated = getSecurityPolicy().isResponseAuthenticatedOnly() || getSecurityPolicy().isResponseAuthenticatedAndEncrypted();
//        boolean shouldBeEncrypted = getSecurityPolicy().isResponseEncryptedOnly() || getSecurityPolicy().isResponseAuthenticatedAndEncrypted();
//
//        if (!authenticatedResponse && shouldBeAuthenticated) {
//            throw new ProtocolException("Received a response that has no authentication tag, while the configured security level states that all responses must be authenticated. Aborting.");
//        }
//        if (!encryptedResponse && shouldBeEncrypted) {
//            throw new ProtocolException("Received an unencrypted response, while the configured security level states that all responses must be encrypted. Aborting.");
//        }
//
//        IrreversibleKey ak = IrreversibleKey.fromByteArray(getSecurityProvider().getAuthenticationKey());
//        IrreversibleKey ek = IrreversibleKey.fromByteArray(getSecurityProvider().getGlobalKey());
//        try {
//            if (!authenticatedResponse && !encryptedResponse) {
//                if (XdlmsApduTags.isGlobalCipheringTag(cipherFrame[0]) || XdlmsApduTags.isDedicatedCipheringTag(cipherFrame[0])) {
//                    //An unsecured global-ciphering or dedicated-ciphering APDU. Unwrap it by stripping of the header.
//                    cipherFrame = ProtocolTools.getSubArray(cipherFrame, 7);
//                }
//
//                return cipherFrame;
//            } else if (authenticatedResponse && !encryptedResponse) {
//                //Authentication only
//                byte[] authTag = getAuthenticationTag(cipherFrame);
//                byte[] apdu = getApdu(cipherFrame, true);
//                byte[] iv = getRespondingInitializationVector();
//
//                ProtocolService.INSTANCE.get().verifyApduAuthentication(apdu, authTag, iv, ak, ek);
//                return apdu;
//            } else if (!authenticatedResponse) {
//                //Encryption only
//                byte[] cipheredAPDU = getApdu(cipherFrame, false);
//                byte[] iv = getRespondingInitializationVector();
//
//                return ProtocolService.INSTANCE.get().decryptApdu(cipheredAPDU, iv, ak, ek);
//            } else {
//                //Both authentication and encryption
//                byte[] cipheredAPDU = getApdu(cipherFrame, true);
//                byte[] iv = getRespondingInitializationVector();
//                byte[] authTag = getAuthenticationTag(cipherFrame);
//
//                return ProtocolService.INSTANCE.get().verifyAuthenticationDecryptApdu(cipheredAPDU, authTag, iv, ak, ek);
//            }
//        } catch (HsmException e) {
//            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
//        }
        //TODO: make available connexo hsmService
        return null;
    }

    @Override
    public byte[] applyGeneralSigning(byte[] securedRequest, ECCCurve eccCurve, byte[] dateTime, byte[] otherInfo, boolean includeRequestLength) throws UnsupportedException {
//        byte[] generalCipheringHeader = createGeneralCipheringHeader(dateTime, otherInfo);
//        byte[] requestData = includeRequestLength ? ProtocolTools.concatByteArrays(DLMSUtils.getAXDRLengthEncoding(securedRequest.length), securedRequest) : securedRequest;
//        byte[] dataToSign = ProtocolTools.concatByteArrays(generalCipheringHeader, requestData);
//        final KeyLabel label = new KeyLabel(getGeneralCipheringSecurityProvider().getClientPrivateSigningKeyLabel());
//        byte[] signature = new byte[0];
//        try {
//            signature = ProtocolService.INSTANCE.get().cosemGenerateSignature(getAtosSecuritySuite(), label, dataToSign);
//        } catch (FunctionFailedException | HsmException e) {
//            throw new UnsupportedException("Unable to sign data using HSM! "+e.getMessage());
//        }
//
//        return ProtocolTools.concatByteArrays(
//                generalCipheringHeader,
//                DLMSUtils.getAXDRLengthEncoding(securedRequest.length),
//                securedRequest,
//                DLMSUtils.getAXDRLengthEncoding(signature.length),
//                signature
//        );
        //TODO: make available connexo hsmService
        return null;
    }

    @Override
    public byte[] unwrapGeneralSigning(byte[] generalSigningAPDU) throws UnsupportedException, ConnectionException {
        throw DeviceConfigurationException.unsupportedPropertyValueWithReason("EncryptionAccessLevel", String.valueOf(getSecurityPolicy().getDataTransportSecurityLevel()), "Checking the signature of responses (ECDSA) is not yet supported");
    }

    private com.atos.worldline.jss.api.custom.energy.SecuritySuite getAtosSecuritySuite() {
        return com.atos.worldline.jss.api.custom.energy.SecuritySuite.SUITE1;
    }
}