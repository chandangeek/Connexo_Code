package com.elster.jupiter.hsm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmIrreversibleKey;
import com.elster.jupiter.hsm.model.response.protocols.DataAndAuthenticationTag;
import com.elster.jupiter.hsm.model.response.protocols.KeyRenewalAgree2EGenerateResponse;
import com.elster.jupiter.hsm.model.response.protocols.KeyRenewalMBusResponse;
import com.elster.jupiter.hsm.model.response.protocols.MacResponse;

import javax.net.ssl.X509KeyManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

@ProviderType
public interface HsmProtocolService {

    byte[] generateDigestMD5(byte[] challenge, HsmIrreversibleKey hlsSecret) throws HsmBaseException;

    byte[] generateDigestSHA1(byte[] challenge, HsmIrreversibleKey hlsSecret) throws HsmBaseException;

    byte[] generateDigestGMAC(byte[] challenge, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] generateDigestMechanism6(boolean isServerToClient, HsmIrreversibleKey hlsSecret, byte[] systemTitleClient, byte[] systemTitleServer, byte[] challengeServerToClient, byte[] challengeClientToServer) throws HsmBaseException;

    @Deprecated
    /**
     * this method is not working in case of General ciphering. Instead HsmProtocolService#authenticateApduWithAAD should be used
     */
    byte[] authenticateApdu(byte[] apdu, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] encryptApdu(byte[] apdu, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException;

    @Deprecated
    /**
     * this method is not working in case of General ciphering. Instead HsmProtocolService#authenticateEncryptApduWithAAD should be used
     */
    DataAndAuthenticationTag authenticateEncryptApdu(byte[] apdu, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite)
            throws HsmBaseException;

    @Deprecated
    /**
     * this method is not working in case of General ciphering. Instead HsmProtocolService#verifyApduAuthenticationWithAAD should be used
     */
    void verifyApduAuthentication(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite)
            throws HsmBaseException;

    byte[] decryptApdu(byte[] apdu, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException;

    @Deprecated
    /**
     * this method is not working in case of General ciphering. Instead HsmProtocolService#verifyAuthenticationDecryptApduWithAAD should be used
     */
    byte[] verifyAuthenticationDecryptApdu(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, HsmIrreversibleKey gak,
                                           HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] authenticateApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException;

    DataAndAuthenticationTag authenticateEncryptApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] verifyAuthenticationDecryptApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] authenticationTag, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] wrapMeterKeyForConcentrator(HsmIrreversibleKey meterKey, HsmIrreversibleKey concentratorKey) throws HsmBaseException;

    byte[] generateRandom(int length) throws HsmBaseException;

    void verifyApduAuthenticationWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] authenticationTag, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] cosemGenerateSignature(int securitySuite, String keyLabel, byte[] dataToSign) throws HsmBaseException;

    /**
     * Verifies the challenge response returned by the meter when invoking the get_framecounter method on the authenticated frame counter IC. This ensures the frame
     * counter received from the server (meter) is authentic.
     *
     * @param serverSysT   Server system title (meter).
     * @param clientSysT   Client system title (comserver).
     * @param challenge    The challenge sent to the server.
     * @param framecounter The frame counter returned by the meter.
     * @param gak          The GAK for the client whose frame counter we requested.
     * @throws HsmBaseException If an error occurs during the HSM operations.
     * @param    challengeResponse    The challenge response received from the server.
     * @return    <code>true</code> if the challenge response is valid, <code>false</code> if the challenge response was not the expected value.
     */
    boolean verifyFramecounterHMAC(final byte[] serverSysT, final byte[] clientSysT, final byte[] challenge, final long framecounter, final HsmIrreversibleKey gak, final byte[] challengeResponse) throws HsmBaseException;

    com.elster.jupiter.hsm.model.response.protocols.EEKAgreeResponse eekAgreeSender1e1s(int securitySuite, String hesSignatureKeyLabel, Certificate[] deviceKeyAgreementKeyCertChain, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmBaseException;

    HsmIrreversibleKey eekAgreeReceiver1e1s(int securitySuite, Certificate[] deviceSignatureKeyCertChain, byte[] ephemeralKaKey, byte[] signature, String hesKaKeyLabel, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmBaseException;

    KeyRenewalAgree2EGenerateResponse keyRenewalAgree2EGenerate(int securitySuite, int keyIDForAgreement, String privateEccSigningKeyLabel, String mdmStorageKeyLabel) throws HsmBaseException;

    HsmIrreversibleKey keyRenewalAgree2EFinalise(int securitySuite, int keyIDForAgree, byte[] serializedPrivateEccKey, byte[] ephemeralEccPubKeyForSmAgreementData, byte[] signature, String caCertificateLabel, Certificate[] certificateChain, byte[] otherInfo, String storageKeyLabel) throws HsmBaseException;

    X509KeyManager getKeyManager(KeyStore keyStore, char[] password, String clientTlsPrivateKeyAlias, X509Certificate[] certificateChain) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException;

    KeyRenewalMBusResponse renewMBusUserKey(byte[] method7apdu, byte[] initializationVector, HsmIrreversibleKey authKey, HsmIrreversibleKey encrKey, HsmIrreversibleKey defaultKey, int securitySuite) throws HsmBaseException;
    KeyRenewalMBusResponse renewMBusFuakWithGCM(String workingKeyLabel, HsmIrreversibleKey defaultKey, byte[] mBusInitialVector) throws HsmBaseException;
    KeyRenewalMBusResponse renewMBusUserKeyWithGCM(HsmIrreversibleKey encrKey, byte[] apduTemplate, byte[] eMeterIV, HsmIrreversibleKey authKey, HsmIrreversibleKey defaultKey, byte[] mbusIV, int securitySuite)throws HsmBaseException;
    MacResponse generateMacFirstBlock(HsmIrreversibleKey firmwareUpdateAuthKey, byte[] clearData) throws HsmBaseException;
    MacResponse generateMacMiddleBlock(HsmIrreversibleKey firmwareUpdateAuthKey, byte[] clearData, byte[] state) throws HsmBaseException;
    MacResponse generateMacLastBlock(HsmIrreversibleKey firmwareUpdateAuthKey, byte[] clearData, byte[] icv, byte[] state) throws HsmBaseException;
    MacResponse generateMacSingleBlock(HsmIrreversibleKey firmwareUpdateAuthKey, byte[] clearData, byte[] icv) throws HsmBaseException;
}
