/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.encryption.kdf;

import com.energyict.encryption.AlgorithmID;

import java.security.MessageDigest;
import java.util.Objects;

/**
 * KDF function as specified by NIST SP 800-56A. Singleton since it's stateless and does not need any dependencies.
 *
 * @author alex
 */
public final class NIST_SP_800_56_KDF implements KDF {

    /**
     * The sole instance.
     */
    private static final NIST_SP_800_56_KDF INSTANCE = new NIST_SP_800_56_KDF();

    /**
     * The maximum number of repetitions.
     */
    private static final long MAX_REPETITIONS = ((1l << 32) - 1);

    /**
     * The maximum hash input length.
     */
    private static final long MAX_HASH_INPUT_LENGTH = Long.MAX_VALUE;

    /**
     * Force singleton.
     */
    private NIST_SP_800_56_KDF() {
    }

    /**
     * Returns the sole instance.
     *
     * @return The sole instance.
     */
    public static final KDF getInstance() {
        return INSTANCE;
    }

    /**
     * Updates the array with the current counter value.
     *
     * @param index   The counter.
     * @param counter 4 byte array to put the counter value in.
     */
    private static final void updateCounter(final long index, final byte[] counter) {
        counter[0] = (byte) ((index >> 24) & 0xFF);
        counter[1] = (byte) ((index >> 16) & 0xFF);
        counter[2] = (byte) ((index >> 8) & 0xFF);
        counter[3] = (byte) (index & 0xFF);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized final byte[] derive(final HashFunction hashFunction, final byte[] z, final AlgorithmID algorithmId, final byte[] partyUInfo, final byte[] partyVInfo) {
        final byte[] otherInfo = new byte[Objects.requireNonNull(algorithmId).getEncoded().length + Objects.requireNonNull(partyUInfo).length + Objects.requireNonNull(partyVInfo).length];

        final int keySizeBytes = hashFunction.getKeyDataLength() / 8;
        final int hashSizeBytes = hashFunction.getHashLength() / 8;

        // Create otherInfo, algo_id || partyUInfo || partyVInfo
        System.arraycopy(algorithmId.getEncoded(), 0, otherInfo, 0, algorithmId.getEncoded().length);
        System.arraycopy(partyUInfo, 0, otherInfo, algorithmId.getEncoded().length, partyUInfo.length);
        System.arraycopy(partyVInfo, 0, otherInfo, algorithmId.getEncoded().length + partyUInfo.length, partyVInfo.length);

        long repetitions = keySizeBytes / hashSizeBytes;

        if (keySizeBytes % hashSizeBytes != 0) {
            repetitions++;
        }

        if (repetitions > MAX_REPETITIONS) {
            throw new IllegalStateException("Number of repetitions should be >= [" + MAX_REPETITIONS + "], instead it was [" + repetitions + "] !");
        }

        // Counter is a 4 byte uint32_t, mixed into the hash.
        final byte[] counter = new byte[4];

        if (((counter.length + z.length + otherInfo.length) * 8) > MAX_HASH_INPUT_LENGTH) {
            throw new IllegalStateException("Cannot derive key, length of input exceeds the max hash input length !");
        }

        final MessageDigest digest = hashFunction.getDigest();

        final byte[] key = new byte[keySizeBytes];

        for (int i = 0; i < repetitions; i++) {
            updateCounter(i + 1, counter);

            digest.update(counter);
            digest.update(z);
            digest.update(otherInfo);

            final byte[] hash = digest.digest();

            if (i < repetitions - 1 || keySizeBytes % hashSizeBytes == 0) {
                System.arraycopy(hash, 0, key, digest.getDigestLength() * i, digest.getDigestLength());
            } else {
                System.arraycopy(hash, 0, key, digest.getDigestLength() * i, keySizeBytes % hashSizeBytes);
            }
        }

        return key;
    }
}