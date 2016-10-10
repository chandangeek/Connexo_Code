package com.energyict.dlms;

/**
 * The 3 possible block cipher (encryption) keys that can be used to encrypt the content of a general ciphering APDU.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/02/11
 * Time: 14:38
 */
public enum GeneralCipheringKeyType {

    /**
     * The EK is identified.
     * Subfields:
     * - key-id: 0 = global-unicast-encryption-key, 1 = global-broadcast-encryption-key
     * See {@link IdentifiedKeyTypes}
     */
    IDENTIFIED_KEY(0, "Identified key"),

    /**
     * The EK is transported in the header of the general ciphering APDU, using key wrapping
     * Subfields:
     * - kek-id: 0 = Master key (always)
     * - key-ciphered-data: Randomly generated EK, wrapped with KEK
     */
    WRAPPED_KEY(1, "Wrapped key"),

    /**
     * The EK is agreed by the parties using One Pass Diffie Hellman C(1e, 1s, ECC CDH)
     * Subfields:
     * - key-parameters: Identifier of the key agreement scheme. 1 = C(1e, 1s ECC CDH), 2 =  C(0e,2s ECC CDH)
     * - key-ciphered-data: in case of key-parameters 1: a public key, with a signature. In case of key-parameters 2: empty octetstring.
     */
    AGREED_KEY(2, "Agreed key");

    private int id;
    private String description;

    GeneralCipheringKeyType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static GeneralCipheringKeyType fromDescription(String description) {
        for (GeneralCipheringKeyType cipheringType : values()) {
            if (cipheringType.getDescription().equals(description)) {
                return cipheringType;
            }
        }
        return null;
    }

    public static GeneralCipheringKeyType fromId(int id) {
        for (GeneralCipheringKeyType cipheringType : values()) {
            if (cipheringType.getId() == id) {
                return cipheringType;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public enum IdentifiedKeyTypes {
        GLOBAL_UNICAST_ENCRYPTION_KEY(0),
        GLOBAL_BROADCAST_ENCRYPTION_KEY(1),;

        private final int id;

        IdentifiedKeyTypes(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum WrappedKeyTypes {
        MASTER_KEY(0);

        private final int id;

        WrappedKeyTypes(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum AgreedKeyTypes {
        ECC_CDH_1E1S(1),
        ECC_CDH_0E2S(2);

        private final int id;

        AgreedKeyTypes(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}