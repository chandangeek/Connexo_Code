package com.energyict.mdc.upl.crypto;

import aQute.bnd.annotation.ProviderType;
import com.energyict.protocol.exceptions.HsmException;

import java.security.cert.Certificate;

@ProviderType
public interface HsmProtocolService {

    byte[] generateDigestMD5(byte[] challenge, IrreversibleKey hlsSecret) throws HsmException;

    byte[] generateDigestSHA1(byte[] challenge, IrreversibleKey hlsSecret) throws HsmException;

    byte[] generateDigestGMAC(byte[] challenge, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException;

    byte[] generateDigestMechanism6(boolean isServerToClient, IrreversibleKey hlsSecret, byte[] systemTitleClient, byte[] systemTitleServer, byte[] challengeServerToClient, byte[] challengeClientToServer) throws HsmException;

    @Deprecated
    /**
     * this method is not working in case of General ciphering. Instead HsmProtocolService#authenticateApduWithAAD should be used
     */
    byte[] authenticateApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException;

    byte[] authenticateApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException;

    byte[] encryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException;

    @Deprecated
    /**
     * this method is not working in case of General ciphering. Instead HsmProtocolService#authenticateEncryptApduWithAAD should be used
     */
    DataAndAuthenticationTag authenticateEncryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite)
            throws HsmException;

    DataAndAuthenticationTag authenticateEncryptApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite)
            throws HsmException;

    @Deprecated
    /**
     * this method is not working in case of General ciphering. Instead HsmProtocolService#verifyApduAuthenticationWithAAD should be used
     */
    void verifyApduAuthentication(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite)
            throws HsmException;

    void verifyApduAuthenticationWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException;

    byte[] decryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException;

    @Deprecated
    /**
     * this method is not working in case of General ciphering. Instead HsmProtocolService#verifyAuthenticationDecryptApduWithAAD should be used
     */
    byte[] verifyAuthenticationDecryptApdu(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak,
                                           IrreversibleKey guek, int securitySuite) throws HsmException;

    byte[] verifyAuthenticationDecryptApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak,
                                                  IrreversibleKey guek, int securitySuite) throws HsmException;

    byte[] wrapMeterKeyForConcentrator(IrreversibleKey meterKey, IrreversibleKey concentratorKey) throws HsmException;

    byte[] generateRandom(int length) throws HsmException;

    byte[] cosemGenerateSignature(int securitySuite, String keyLabel, byte[] dataToSign) throws HsmException;

    /**
     * Verifies the challenge response returned by the meter when invoking the get_framecounter method on the authenticated frame counter IC. This ensures the frame
     * counter received from the server (meter) is authentic.
     *
     * @param serverSysT   Server system title (meter).
     * @param clientSysT   Client system title (comserver).
     * @param challenge    The challenge sent to the server.
     * @param framecounter The frame counter returned by the meter.
     * @param gak          The GAK for the client whose frame counter we requested.
     * @throws HsmException If an error occurs during the HSM operations.
     * @param    challengeResponse    The challenge response received from the server.
     * @return    <code>true</code> if the challenge response is valid, <code>false</code> if the challenge response was not the expected value.
     */
    boolean verifyFramecounterHMAC(final byte[] serverSysT, final byte[] clientSysT, final byte[] challenge, final long framecounter, final IrreversibleKey gak, final byte[] challengeResponse) throws HsmException;

    EEKAgreeResponse eekAgreeSender1e1s(int securitySuite, String hesSignatureKeyLabel, Certificate[] deviceKeyAgreementKeyCertChain, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmException;

    IrreversibleKey eekAgreeReceiver1e1s(int securitySuite, Certificate[] deviceSignatureKeyCertChain, byte[] ephemeralKaKey, byte[] signature, String hesKaKeyLabel, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmException;

    KeyRenewalAgree2EGenerateResponse keyRenewalAgree2EGenerate(int securitySuite, int keyIDForAgreement, String privateEccSigningKeyLabel, String mdmStorageKeyLabel) throws HsmException;

    IrreversibleKey keyRenewalAgree2EFinalise(int securitySuite, int keyIDForAgree, byte[] serializedPrivateEccKey, byte[] ephemeralEccPubKeyForSmAgreementData, byte[] signature, String caCertificateLabel, Certificate[] certificateChain, byte[] otherInfo, String storageKeyLabel) throws HsmException;

    KeyRenewalMBusResponse renewMBusUserKey(byte[] method7apdu, byte[] initializationVector, IrreversibleKey authKey, IrreversibleKey encrKey, IrreversibleKey defaultKey, int securitySuite) throws HsmException;
    KeyRenewalMBusResponse renewMBusFuakWithGCM(String workingKeyLabel, IrreversibleKey defaultKey, byte[] mBusInitialVector) throws HsmException;
    KeyRenewalMBusResponse renewMBusUserKeyWithGCM(IrreversibleKey encrKey, byte[] apduTemplate, byte[] eMeterIV, IrreversibleKey authKey, IrreversibleKey defaultKey, byte[] mbusIV, int securitySuite) throws HsmException;
    MacResponse generateMacFirstBlock(IrreversibleKey firmwareUpdateAuthKey, byte[] clearData) throws HsmException;
    MacResponse generateMacMiddleBlock(IrreversibleKey firmwareUpdateAuthKey, byte[] clearData, byte[] state) throws HsmException;
    MacResponse generateMacLastBlock(IrreversibleKey firmwareUpdateAuthKey, byte[] clearData, byte[] icv, byte[] state) throws HsmException;
    MacResponse generateMacSingleBlock(IrreversibleKey firmwareUpdateAuthKey, byte[] clearData, byte[] icv) throws HsmException;

    byte[] wrapServiceKey(byte[] preparedData, byte[] signature, String verifyKey);
}
