package com.energyict.protocolimplv2.umi.session;

import com.energyict.protocolimplv2.umi.security.scheme2.UmiCVCCertificate;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;

public class UmiSessionS2Utils {
    public static final int AES_128_GCM_KEY_LENGTH = 16;
    public static final int SALT_LENGTH = 16;
    public static final String KEY_AGREEMENT_ALGORITHM = "ECDH";
    public static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Establishes a session key
     * (1) calculate master key via ECDH using remote public key & our private key
     * (2) derive session key from the master key using HMAC-SHA-256-128 on the (randomly generated) salt.
     *
     * @param saltA Device A salt sent in S2_START_SESSION
     * @param saltB Device B salt sent in S2_START_SESSION_RESPONSE
     * @return session key
     */
    public static byte[] createSessionKey(PrivateKey localPrivateKey, UmiCVCCertificate remoteCert, byte[] saltA, byte[] saltB)
            throws GeneralSecurityException {
        try {
            byte[] mergedSalts = new byte[saltA.length + saltB.length];
            System.arraycopy(saltA, 0, mergedSalts, 0, saltA.length);
            System.arraycopy(saltB, 0, mergedSalts, saltA.length, saltB.length);

            KeyAgreement agreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM);
            agreement.init(localPrivateKey);
            agreement.doPhase(remoteCert.getPublicKey(), true);

            byte[] sharedSecret = agreement.generateSecret();
            Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secret_key = new SecretKeySpec(sharedSecret, HMAC_SHA256);
            sha256_HMAC.init(secret_key);
            return Arrays.copyOfRange(sha256_HMAC.doFinal(mergedSalts), 0, AES_128_GCM_KEY_LENGTH);
        } catch (Exception e) {
            throw new GeneralSecurityException(e.getMessage());
        }
    }

    /**
     * Generate salt for start session UMI S2 command.
     *
     * @return salt
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new Random().nextBytes(salt);
        return salt;
    }
}
