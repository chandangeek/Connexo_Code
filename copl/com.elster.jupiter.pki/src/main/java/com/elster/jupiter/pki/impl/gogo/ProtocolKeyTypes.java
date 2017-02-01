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
public enum ProtocolKeyTypes implements KeyTypeCreator {
    AES_128 {
        public String getName() {
            return "AES 128";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.addSymmetricKeyType(getName(), "AES", 128);
        }
    },
    AES_192 {
        public String getName() {
            return "AES 192";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.addSymmetricKeyType(getName(), "AES", 192);
        }
    },
    AES_256 {
        public String getName() {
            return "AES 256";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.addSymmetricKeyType(getName(), "AES", 256);
        }
    },
    DES {
        public String getName() {
            return "DES";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.addSymmetricKeyType(getName(), "DES", 64);
        }
    },
    SECP256R1 {
        public String getName() {
            return "NIST P-256";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.addAsymmetricKeyType(getName()).EC().curve("secp256r1").add();
        }

    },
    SECP384R1 {
        public String getName() {
            return "NIST P-384";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.addAsymmetricKeyType(getName()).EC().curve("secp384r1").add();
        }
    },
    ;

}
