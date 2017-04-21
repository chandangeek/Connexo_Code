/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.gogo;

import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.PkiService;

import java.util.EnumSet;

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
    TRUSTED_ROOT {
        public String getName() {
            return "Trusted root";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newTrustedCertificateType(getName()).description("Created by test class").add();
        }
    },
    PASSWORD {
        public String getName() {
            return "Password";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newPassphraseType(getName()).length(20).withLowerCaseCharacters().withUpperCaseCharacters().add();
        }
    },
    TLS_CLIENT_SUITE_1 {
        @Override
        public String getName() {
            return "DLMS TLS Client suite 1";
        }

        @Override
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService
                    .newClientCertificateType(getName(), "SHA256withECDSA")
                    .description("DLMS TLS SUITE 1")
                    .setKeyUsages(EnumSet.of(KeyUsage.keyAgreement, KeyUsage.keyCertSign))
                    .setExtendedKeyUsages(EnumSet.of(ExtendedKeyUsage.tlsWebClientAuthentication, ExtendedKeyUsage.tlsWebServerAuthentication))
                    .ECDSA()
                    .curve("secp256r1")
                    .add();
        }
    },
    TLS_CLIENT_SUITE_2 {
        @Override
        public String getName() {
            return "DLMS TLS Client suite 2";
        }

        @Override
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService
                    .newClientCertificateType(getName(), "SHA256withECDSA")
                    .description("DLMS TLS SUITE 2")
                    .setKeyUsages(EnumSet.of(KeyUsage.keyAgreement, KeyUsage.keyCertSign))
                    .setExtendedKeyUsages(EnumSet.of(ExtendedKeyUsage.tlsWebClientAuthentication, ExtendedKeyUsage.tlsWebServerAuthentication))
                    .ECDSA()
                    .curve("secp384r1")
                    .add();
        }
    }
    ;

    abstract public String getName();
    abstract public KeyType createKeyType(PkiService pkiService);

}
