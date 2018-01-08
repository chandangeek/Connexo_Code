package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.time.TimeDuration;

public enum KeyAccessorTpl {
    AUTHENTICATION_KEY("AuthenticationKey",
            TimeDuration.years(1),
            buildSymmetricKeyType("AuthenticationKey", "AES", 128)
    ),
    ENCRYPTION_KEY("EncryptionKey",
            TimeDuration.years(1),
            buildSymmetricKeyType("EncryptionKey", "AES", 128)
    ),
    PASSWORD("Password",
            TimeDuration.years(1),
            buildPassphraseType("Password", 30)
    ),
    AUTHENTICATION_KEY_BEACON1("AuthenticationKeyBeacon1",
            TimeDuration.months(1),
            buildSymmetricKeyType("AuthenticationKeyBeacon1", "AES", 128)
    ),
    AUTHENTICATION_KEY_BEACON2("AuthenticationKeyBeacon2",
            TimeDuration.years(10),
            buildSymmetricKeyType("AuthenticationKeyBeacon2", "AES", 128)
    ),
    ENCRYPTION_KEY_BEACON1("EncryptionKeyBeacon1",
            TimeDuration.years(10),
            buildSymmetricKeyType("EncryptionKeyBeacon1", "AES", 128)
    ),
    ENCRYPTION_KEY_BEACON2("EncryptionKeyBeacon2",
            TimeDuration.years(10),
            buildSymmetricKeyType("EncryptionKeyBeacon2", "AES", 128)
    ),
    PSK("PSK",
            TimeDuration.years(10),
            buildSymmetricKeyType("PSKEncryptionKey1", "AES", 128)
    ),
    PSK_ENCRYPTION_KEY1("PSKEncryptionKey1",
            TimeDuration.years(10),
            buildSymmetricKeyType("PSKEncryptionKey1", "AES", 128)
    ),
    PSK_ENCRYPTION_KEY2("PSKEncryptionKey2",
            TimeDuration.years(10),
            buildSymmetricKeyType("PSKEncryptionKey2", "AES", 128)
    ),;

    private String name;
    private TimeDuration timeDuration;
    private KeyType keyType;

    KeyAccessorTpl(String name, TimeDuration timeDuration, KeyType keyType) {
        this.name = name;
        this.timeDuration = timeDuration;
        this.keyType = keyType;
    }

    public String getName() {
        return name;
    }

    public TimeDuration getTimeDuration() {
        return timeDuration;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    private static KeyType buildPassphraseType(String name, Integer specialCharacters) {
        return new KeyType(name, specialCharacters);
    }

    private static KeyType buildSymmetricKeyType(String name, String keyAlgorithmName, Integer keySize) {
        return new KeyType(name, keyAlgorithmName, keySize);
    }

    public static class KeyType {
        private String name;
        private Integer specialCharacters;
        private String keyAlgorithmName;
        private Integer keySize;

        KeyType(String name, String keyAlgorithmName, Integer keySize) {
            this.name = name;
            this.keyAlgorithmName = keyAlgorithmName;
            this.keySize = keySize;
        }

        KeyType(String name, Integer specialCharacters) {
            this.name = name;
            this.specialCharacters = specialCharacters;
        }

        public String getName() {
            return name;
        }

        public Integer getSpecialCharacters() {
            return specialCharacters;
        }

        public String getKeyAlgorithmName() {
            return keyAlgorithmName;
        }

        public Integer getKeySize() {
            return keySize;
        }
    }
}
