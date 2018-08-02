package com.energyict.mdc.upl.crypto;

import com.energyict.protocol.exceptions.HsmException;

public interface HsmProtocolService {

    byte[] generateDigestMD5(byte[] challenge, IrreversibleKey hlsSecret) throws HsmException;

    byte[] generateDigestSHA1(byte[] challenge, IrreversibleKey hlsSecret) throws HsmException;

    byte[] generateDigestGMAC(byte[] challenge, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmException;

    byte[] authenticateApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmException;

    byte[] encryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmException;

    DataAndAuthenticationTag authenticateEncryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek)
            throws HsmException;

    void verifyApduAuthentication(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek)
            throws HsmException;

    byte[] decryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmException;

    byte[] verifyAuthenticationDecryptApdu(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak,
                                           IrreversibleKey guek) throws HsmException;

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
}
