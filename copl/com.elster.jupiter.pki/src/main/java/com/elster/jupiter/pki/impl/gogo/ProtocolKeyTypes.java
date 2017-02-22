/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.gogo;

import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;

/**
 * This enum is prototype KeyType creator/referencer, as could be made by protocols
 *
 * Protocols will create the KeyTypes it requires itself, with this enum, created keytypes can be retrieved by name
 * afterwards
 */
public enum ProtocolKeyTypes {
    AES_128 {
        public String getName() {
            return "AES 128";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newSymmetricKeyType(getName(), "AES", 128).description("Created by test class").add();
        }
    },
    AES_192 {
        public String getName() {
            return "AES 192";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newSymmetricKeyType(getName(), "AES", 192).description("Created by test class").add();
        }
    },
    AES_256 {
        public String getName() {
            return "AES 256";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newSymmetricKeyType(getName(), "AES", 256).description("Created by test class").add();
        }
    },
    DES {
        public String getName() {
            return "DES";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newSymmetricKeyType(getName(), "DES", 64).description("Created by test class").add();
        }
    },
    SECP256R1 {
        public String getName() {
            return "NIST P-256";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newAsymmetricKeyType(getName()).ECDSA().curve("secp256r1").add();
        }

    },
    SECP384R1 {
        public String getName() {
            return "NIST P-384";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newAsymmetricKeyType(getName()).ECDSA().curve("secp384r1").add();
        }
    },
    ;

    abstract public String getName();
    abstract public KeyType createKeyType(PkiService pkiService);

}
