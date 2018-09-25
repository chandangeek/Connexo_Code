package com.elster.jupiter.hsm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.IrreversibleKey;
import com.elster.jupiter.hsm.model.response.protocols.DataAndAuthenticationTag;

import javax.net.ssl.X509KeyManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;

@ProviderType
public interface HsmProtocolService {

    byte[] generateDigestMD5(byte[] challenge, IrreversibleKey hlsSecret) throws HsmBaseException;

    byte[] generateDigestSHA1(byte[] challenge, IrreversibleKey hlsSecret) throws HsmBaseException;

    byte[] generateDigestGMAC(byte[] challenge, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] authenticateApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] encryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException;

    DataAndAuthenticationTag authenticateEncryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite)
            throws HsmBaseException;

    void verifyApduAuthentication(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite)
            throws HsmBaseException;

    byte[] decryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] verifyAuthenticationDecryptApdu(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak,
                                           IrreversibleKey guek, int securitySuite) throws HsmBaseException;

    byte[] wrapMeterKeyForConcentrator(IrreversibleKey meterKey, IrreversibleKey concentratorKey) throws HsmBaseException;

    byte[] generateRandom(int length) throws HsmBaseException;

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
    boolean verifyFramecounterHMAC(final byte[] serverSysT, final byte[] clientSysT, final byte[] challenge, final long framecounter, final IrreversibleKey gak, final byte[] challengeResponse) throws HsmBaseException;

    com.elster.jupiter.hsm.model.response.protocols.EEKAgreeResponse eekAgreeSender1e1s(int securitySuite, String hesSignatureKeyLabel, Certificate[] deviceKeyAgreementKeyCertChain, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmBaseException;

    IrreversibleKey eekAgreeReceiver1e1s(int securitySuite, Certificate[] deviceSignatureKeyCertChain, byte[] ephemeralKaKey, byte[] signature, String hesKaKeyLabel, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmBaseException;

    X509KeyManager getKeyManager(KeyStore keyStore, char[] password) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException;
}
