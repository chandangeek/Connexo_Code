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
import com.energyict.encryption.asymetric.util.KeyUtils;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.crypto.DataAndAuthenticationTag;
import com.energyict.mdc.upl.crypto.EEKAgreeResponse;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exceptions.HsmException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.CryptoBeacon3100SecurityProvider;
import com.energyict.protocolimplv2.security.SecurityPropertySpecTranslationKeys;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Objects;

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
            IrreversibleKey ek = IrreversibleKeyImpl.fromByteArray(getEncryptionKey(false));
            if (getSecurityPolicy().isRequestAuthenticatedOnly()) {
                tag = Services.hsmService().authenticateApduWithAAD(plainText, createGeneralCipheringHeaderIfNeeded(), getInitializationVector(), ak, ek, getSecuritySuite());
            } else if (getSecurityPolicy().isRequestEncryptedOnly()) {
                encryptedRequest = Services.hsmService().encryptApdu(plainText, getInitializationVector(), ak, ek, getSecuritySuite());
            } else if (getSecurityPolicy().isRequestAuthenticatedAndEncrypted()) {
                DataAndAuthenticationTag dataAndAuthenticationTag = Services.hsmService().authenticateEncryptApduWithAAD(plainText, createGeneralCipheringHeaderIfNeeded(), getInitializationVector(), ak, ek, getSecuritySuite());
                encryptedRequest = dataAndAuthenticationTag.getData();
                tag = dataAndAuthenticationTag.getAuthenticationTag();
            } else {
                throw new UnsupportedException("Unknown securityPolicy: " + this.getSecurityPolicy().getDataTransportSecurityLevel());
            }
            return createSecuredApdu(encryptedRequest == null ? plainText : encryptedRequest, tag);
        } catch (HsmException e) {
            throw ConnectionCommunicationException.unexpectedHsmProtocolError(new NestedIOException(e));
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
        IrreversibleKey ek = IrreversibleKeyImpl.fromByteArray(getEncryptionKey(true));
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

                Services.hsmService().verifyApduAuthenticationWithAAD(apdu, generalCipheringHeader, authTag, iv, ak, ek, getSecuritySuite());
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

                return Services.hsmService().verifyAuthenticationDecryptApduWithAAD(cipheredAPDU, generalCipheringHeader, authTag, iv, ak, ek, getSecuritySuite());
            }
        } catch (HsmException e) {
            throw ConnectionCommunicationException.unexpectedHsmProtocolError(new NestedIOException(e));
        }
    }

    @Override
    public byte[] applyGeneralSigning(byte[] securedRequest, ECCCurve eccCurve, byte[] dateTime, byte[] otherInfo, boolean includeRequestLength) {
        byte[] generalCipheringHeader = createGeneralCipheringHeader(dateTime, otherInfo);
        byte[] requestData = includeRequestLength ? ProtocolTools.concatByteArrays(DLMSUtils.getAXDRLengthEncoding(securedRequest.length), securedRequest) : securedRequest;
        byte[] dataToSign = ProtocolTools.concatByteArrays(generalCipheringHeader, requestData);
        final String keyLabel = getGeneralCipheringSecurityProvider().getClientPrivateSigningKeyLabel();
        byte[] signature;
        try {
            signature = Services.hsmService().cosemGenerateSignature(SECURITY_SUITE_1, keyLabel, dataToSign);
        } catch (HsmException e) {
            throw ConnectionCommunicationException.unexpectedHsmProtocolError(new NestedIOException(e, "Unable to sign data using HSM! " + e.getMessage()));
        }

        return ProtocolTools.concatByteArrays(
                generalCipheringHeader,
                DLMSUtils.getAXDRLengthEncoding(securedRequest.length),
                securedRequest,
                DLMSUtils.getAXDRLengthEncoding(signature.length),
                signature
        );
    }

    protected byte[] dataTransportGeneralEncryptionWithKeyAgreement(byte[] plainText) throws UnsupportedException {
        Certificate serverKeyAgreementCertificate = getGeneralCipheringSecurityProvider().getServerKeyAgreementCertificate();
        if (serverKeyAgreementCertificate == null) {
            throw DeviceConfigurationException.missingProperty(SecurityPropertySpecTranslationKeys.SERVER_KEY_AGREEMENT_CERTIFICATE.toString());
        }

        String clientPrivateSigningKeyLabel = getGeneralCipheringSecurityProvider().getClientPrivateSigningKeyLabel();
        Certificate[] serverKeyAgreementCertificateChain = getGeneralCipheringSecurityProvider().getCertificateChain(SecurityPropertySpecTranslationKeys.SERVER_KEY_AGREEMENT_CERTIFICATE.toString());
        String caCertificate = getGeneralCipheringSecurityProvider().getRootCAAlias(SecurityPropertySpecTranslationKeys.SERVER_KEY_AGREEMENT_CERTIFICATE.toString());
        final byte[] kdfOtherInfo = getKdfOtherInfo(getSystemTitle(), getResponseSystemTitle());

        String storageKey = ((CryptoBeacon3100SecurityProvider) getSecurityProvider()).getEekStorageLabel();

        //call hsm eekAgreeSender1e1s method.
        EEKAgreeResponse eekAgreeResponse = Services.hsmService().eekAgreeSender1e1s(getSecuritySuite(), clientPrivateSigningKeyLabel, serverKeyAgreementCertificateChain, caCertificate, kdfOtherInfo, storageKey);

        byte[] sessionKey = eekAgreeResponse.getEek().toBase64ByteArray();//will be retrieved from hsm

        getGeneralCipheringSecurityProvider().setSessionKey(sessionKey);
        byte[] ephemeralPublicKeyBytes = eekAgreeResponse.getEphemeralPublicKey();//will be retrieved from hsm
        byte[] signature = eekAgreeResponse.getSignature();//will be retrieved from hsm

        return createKeyAgreementRequest(plainText, ephemeralPublicKeyBytes, signature);
    }

    protected int dataTransportGeneralDecryptionForAgreedKey(byte[] generalCipheringAPDU, int ptr) throws UnsupportedException {
        int keyParametersLength = generalCipheringAPDU[ptr++] & 0xFF;
        int keyParameters = generalCipheringAPDU[ptr++] & 0xFF;

        if (GeneralCipheringKeyType.AgreedKeyTypes.ECC_CDH_1E1S.getId() != keyParameters) {
            throw new UnsupportedException("Unsupported key agreement type: '" + keyParameters + "'. Only type 1 (1e, 1s, ECC CDH) is currently supported");
        }

        int keyCipheredDataLength = DLMSUtils.getAXDRLength(generalCipheringAPDU, ptr);
        ptr += DLMSUtils.getAXDRLengthOffset(keyCipheredDataLength);

        byte[] keyCipheredData = ProtocolTools.getSubArray(generalCipheringAPDU, ptr, ptr + keyCipheredDataLength);
        ptr += keyCipheredDataLength;

        int keySize = KeyUtils.getKeySize(getECCCurve());
        byte[] serverEphemeralPublicKeyBytes = ProtocolTools.getSubArray(keyCipheredData, 0, keySize);
        byte[] signature = ProtocolTools.getSubArray(keyCipheredData, keySize, keyCipheredDataLength);

        X509Certificate serverSignatureCertificate = getGeneralCipheringSecurityProvider().getServerSignatureCertificate();
        if (serverSignatureCertificate == null) {
            throw DeviceConfigurationException.missingProperty(SecurityPropertySpecTranslationKeys.SERVER_SIGNING_CERTIFICATE.toString());
        }
        Certificate[] serverSignatureKeyCertificateChain = getGeneralCipheringSecurityProvider().getCertificateChain(SecurityPropertySpecTranslationKeys.SERVER_SIGNING_CERTIFICATE.toString());
        String clientPrivateKeyAgreementKeyLabel = getGeneralCipheringSecurityProvider().getClientPrivateKeyAgreementKeyLabel();
        String caCertificate = getGeneralCipheringSecurityProvider().getRootCAAlias(SecurityPropertySpecTranslationKeys.SERVER_SIGNING_CERTIFICATE.toString());
        byte[] kdfOtherInfo = getKdfOtherInfo(getResponseSystemTitle(), getSystemTitle());
        String storageKey = ((CryptoBeacon3100SecurityProvider) getSecurityProvider()).getEekStorageLabel();

        //call hsm eekAgreeReceiver1e1s method.
        IrreversibleKey agreedSesionKey = Services.hsmService().eekAgreeReceiver1e1s(getSecuritySuite(), serverSignatureKeyCertificateChain, serverEphemeralPublicKeyBytes, signature, clientPrivateKeyAgreementKeyLabel, caCertificate, kdfOtherInfo, storageKey);

        handleServerSessionKey(agreedSesionKey.toBase64ByteArray());

        return ptr;
    }

    public byte[] getKdfOtherInfo(byte[] senderSystemTitle, byte[] receiverSystemTitle) {
        final byte[] encodedKeyDerivingEncryptionAlgorithm = getKeyDerivingEncryptionAlgorithm().getEncoded();
        final byte[] kdfOtherInfo = new byte[Objects.requireNonNull(getKeyDerivingEncryptionAlgorithm()).getEncoded().length + Objects.requireNonNull(senderSystemTitle).length + Objects.requireNonNull(receiverSystemTitle).length];

        // Create otherInfo, algo_id || partyUInfo || partyVInfo
        System.arraycopy(encodedKeyDerivingEncryptionAlgorithm, 0, kdfOtherInfo, 0, encodedKeyDerivingEncryptionAlgorithm.length);
        System.arraycopy(senderSystemTitle, 0, kdfOtherInfo, encodedKeyDerivingEncryptionAlgorithm.length, senderSystemTitle.length);
        System.arraycopy(receiverSystemTitle, 0, kdfOtherInfo, encodedKeyDerivingEncryptionAlgorithm.length + senderSystemTitle.length, receiverSystemTitle.length);
        return kdfOtherInfo;
    }

}