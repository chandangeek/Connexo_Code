package com.elster.jupiter.hsm.model.krypto;

import com.elster.jupiter.hsm.model.EncryptBaseException;

public enum SymmetricAlgorithm  {

    AES, DES, DESede, RSA;

    /**
     * @param encryptionSpec as Java standard Cipher string (like AES/NOPADDING/NoPadding, AES/PKCS5/PKCS5Padding)
     * @return enum described by received cipher
     * @throws EncryptBaseException if algorithm could not have been extracted from string
     */
    public static SymmetricAlgorithm from(String encryptionSpec) throws EncryptBaseException {
        String[] split = validate(encryptionSpec);

        return SymmetricAlgorithm.valueOf(split[0]);
    }

    private static String[] validate(String encryptionSpec) throws EncryptBaseException {
        if (encryptionSpec == null || encryptionSpec.isEmpty()) {
            throw new EncryptBaseException("Cowardly refusing to build symmetric algorithm from null or empty string");
        }

        String[] split = encryptionSpec.split("/");
        if (split.length != 3) {
            throw new EncryptBaseException("Cowardly refusing to create symmetric algorithm with wrong input format:" + encryptionSpec + ". When expecting Cipher format (e.g: AES/NOPADDING/NoPadding)");
        }
        return split;
    }
}
