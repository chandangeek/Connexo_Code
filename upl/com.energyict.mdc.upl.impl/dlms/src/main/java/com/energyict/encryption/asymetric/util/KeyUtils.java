package com.energyict.encryption.asymetric.util;

import com.energyict.encryption.asymetric.ECCCurve;
import com.energyict.protocol.exceptions.DataEncryptionException;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidParameterSpecException;

/**
 * Key utilities, conversion utilities and the likes.
 *
 * @author alex
 */
public final class KeyUtils {

    /**
     * The ECC algo.
     */
    private static final String ECC_ALGORITHM = "EC";

    /**
     * The Sun EC provider.
     */
    private static final String SUN_EC_PROVIDER = "SunEC";

    /**
     * Prevent instantiation.
     */
    private KeyUtils() {
    }

    /**
     * Converts the given X || Y raw key data to a {@link PublicKey} object.
     *
     * @param curve   The curve to be used (ie. secp256r1, secp384r1).
     * @param rawData The raw key data.
     * @return The {@link PublicKey} object.
     */
    public static final PublicKey toECPublicKey(final ECCCurve curve, final byte[] rawData) {
        try {
            final ECParameterSpec ecParameters = getEcParameterSpec(curve);
            final int keySize = getKeySize(ecParameters);

            final byte[] x = new byte[keySize / 2 + 1];
            System.arraycopy(rawData, 0, x, 1, keySize / 2);

            final byte[] y = new byte[keySize / 2 + 1];
            System.arraycopy(rawData, keySize / 2, y, 1, keySize / 2);

            final ECPublicKeySpec spec = new ECPublicKeySpec(new ECPoint(new BigInteger(x), new BigInteger(y)), ecParameters);

            final KeyFactory keyFactory = KeyFactory.getInstance(ECC_ALGORITHM);

            return keyFactory.generatePublic(spec);
        } catch (GeneralSecurityException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    public static int getKeySize(ECParameterSpec ecParameters) {
        return 2 * ecParameters.getOrder().bitLength() / Byte.SIZE;
    }

    public static int getKeySize(ECCCurve curve) {
        return 2 * getEcParameterSpec(curve).getOrder().bitLength() / Byte.SIZE;
    }

    public static ECParameterSpec getEcParameterSpec(ECCCurve curve) {
        try {
            final AlgorithmParameters parameters = AlgorithmParameters.getInstance(ECC_ALGORITHM, SUN_EC_PROVIDER);
            parameters.init(new ECGenParameterSpec(curve.getCurveName()));
            return parameters.getParameterSpec(ECParameterSpec.class);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException e) {
            throw DataEncryptionException.dataEncryptionException(e);
        }
    }

    /**
     * Convert the values (x and y) of the public key to FE2OS(x)II FE2OS(y)
     */
    public static byte[] toRawData(final ECCCurve curve, final PublicKey publicKey) {
        ECPoint point = ((ECPublicKey) publicKey).getW();
        BigInteger x = point.getAffineX();
        BigInteger y = point.getAffineY();

        final ECParameterSpec ecParameters = getEcParameterSpec(curve);
        final int keySize = getKeySize(ecParameters);

        byte[] xBytes = getBytesFromBigInteger(x, keySize / 2);
        byte[] yBytes = getBytesFromBigInteger(y, keySize / 2);
        return ProtocolTools.concatByteArrays(xBytes, yBytes);
    }

    private static byte[] getBytesFromBigInteger(BigInteger bigInteger, int length) {
        byte[] result = new byte[length];
        byte[] bytes = bigInteger.toByteArray();

        //If the byte array is too long, cut off the first byte(s). These can be 0x00 bytes.
        if (bytes.length > length) {
            bytes = ProtocolTools.getSubArray(bytes, bytes.length - length);
        }

        //Pad the first positions of the result if the byte array was too short.
        System.arraycopy(bytes, 0, result, length - bytes.length, bytes.length);
        return result;
    }

    /**
     * Converts the given raw key data to a {@link PrivateKey} object.
     *
     * @param curve   The curve to be used (ie. secp256r1, secp384r1).
     * @param rawData The raw key data.
     * @return The {@link PrivateKey} object.
     */
    public static final PrivateKey toECPrivateKey(final ECCCurve curve, final byte[] rawData) {
        try {
            final ECParameterSpec ecParameters = getEcParameterSpec(curve);
            final ECPrivateKeySpec keySpec = new ECPrivateKeySpec(new BigInteger(rawData), ecParameters);

            final KeyFactory keyFactory = KeyFactory.getInstance(ECC_ALGORITHM);

            return keyFactory.generatePrivate(keySpec);
        } catch (GeneralSecurityException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    /**
     * Generates a new ECC key pair.
     *
     * @param curve The curve to be used.
     * @return The key pair.
     * @throws GeneralSecurityException If an error occurs during the key pair generation.
     */
    public static final KeyPair generateECCKeyPair(final ECCCurve curve) throws GeneralSecurityException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(ECC_ALGORITHM, SUN_EC_PROVIDER);
        final ECGenParameterSpec parameterSpec = new ECGenParameterSpec(curve.getCurveName());

        generator.initialize(parameterSpec);

        return generator.generateKeyPair();
    }
}