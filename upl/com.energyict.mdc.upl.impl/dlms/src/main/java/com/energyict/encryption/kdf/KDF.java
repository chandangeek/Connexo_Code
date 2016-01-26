package com.energyict.encryption.kdf;

import com.energyict.encryption.AlgorithmID;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * KDF function.
 *
 * @author alex
 */
public interface KDF {

    /**
     * Performs the derivation.
     *
     * @param z            The shared secret.
     * @param algorithmId  The algo ID.
     * @param partyUInfo   Public information about party U, usually a system title.
     * @param partyVInfo   Public information about party V, usually a system title.
     * @param hashFunction The hash function.
     * @return The key that was derived.
     */
    byte[] derive(final HashFunction hashFunction, final byte[] z, final AlgorithmID algorithmId, final byte[] partyUInfo, final byte[] partyVInfo);

    /**
     * Enumerate the hash functions.
     *
     * @author alex
     */
    enum HashFunction {

        /**
         * SHA-256, used by suite 1.
         */
        SHA256("SHA-256", 256, 128),

        /**
         * SHA-384, used by suite 2.
         */
        SHA384("SHA-384", 384, 256);

        /**
         * The name of the hash function to be used.
         */
        private final String name;

        /**
         * Size the hash, in bits.
         */
        private final int hashLength;

        /**
         * The key data length.
         */
        private final int keyDataLength;

        /**
         * Create a new instance.
         *
         * @param name The name.
         */
        HashFunction(final String name, final int hashLength, final int keyDataLength) {
            this.name = name;
            this.hashLength = hashLength;
            this.keyDataLength = keyDataLength;
        }

        /**
         * Returns a {@link MessageDigest} corresponding to this function.
         *
         * @return The message digest corresponding to this function.
         */
        public final MessageDigest getDigest() {
            try {
                return MessageDigest.getInstance(this.name);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Could not instantiate hashing function : [" + e.getMessage() + "]", e);
            }
        }

        /**
         * Returns the length of the hash, in bits.
         *
         * @return The length of the hash, in bits.
         */
        public final int getHashLength() {
            return this.hashLength;
        }

        /**
         * Returns the key data length.
         *
         * @return The key data length.
         */
        public final int getKeyDataLength() {
            return this.keyDataLength;
        }
    }
}