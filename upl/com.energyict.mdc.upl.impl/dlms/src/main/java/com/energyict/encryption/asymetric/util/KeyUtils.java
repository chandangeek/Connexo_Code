package com.energyict.encryption.asymetric.util;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

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
    public static final PublicKey toECPublicKey(final String curve, final byte[] rawData) {
        try {
            final AlgorithmParameters parameters = AlgorithmParameters.getInstance(ECC_ALGORITHM, SUN_EC_PROVIDER);
            parameters.init(new ECGenParameterSpec(curve));

            final ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
            final int keySize = ecParameters.getOrder().bitLength() / Byte.SIZE;

            final byte[] x = Arrays.copyOfRange(rawData, 0, keySize);
            final byte[] y = Arrays.copyOfRange(rawData, keySize, keySize * 2);

            final ECPublicKeySpec spec = new ECPublicKeySpec(new ECPoint(new BigInteger(x), new BigInteger(y)), ecParameters);

            final KeyFactory keyFactory = KeyFactory.getInstance(ECC_ALGORITHM);

            return keyFactory.generatePublic(spec);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Error converting key data : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Converts the given raw key data to a {@link PrivateKey} object.
     *
     * @param curve   The curve to be used (ie. secp256r1, secp384r1).
     * @param rawData The raw key data.
     * @return The {@link PrivateKey} object.
     */
    public static final PrivateKey toECPrivateKey(final String curve, final byte[] rawData) {
        try {
            final AlgorithmParameters parameters = AlgorithmParameters.getInstance(ECC_ALGORITHM, SUN_EC_PROVIDER);
            parameters.init(new ECGenParameterSpec(curve));

            final ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
            final ECPrivateKeySpec keySpec = new ECPrivateKeySpec(new BigInteger(rawData), ecParameters);

            final KeyFactory keyFactory = KeyFactory.getInstance(ECC_ALGORITHM);

            return keyFactory.generatePrivate(keySpec);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Error converting key data : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Generates a new ECC key pair.
     *
     * @param curve The curve to be used.
     * @return The key pair.
     * @throws GeneralSecurityException If an error occurs during the key pair generation.
     */
    public static final KeyPair generateECCKeyPair(final String curve) throws GeneralSecurityException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(ECC_ALGORITHM, SUN_EC_PROVIDER);
        final ECGenParameterSpec parameterSpec = new ECGenParameterSpec(curve);

        generator.initialize(parameterSpec);

        return generator.generateKeyPair();
    }
}