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
            return pkiService.newSymmetricKeyType(getName(), "AES", 128).description("An 128 bit key suited for AES encryption").add();
        }
    },
    AES_192 {
        public String getName() {
            return "AES 192";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newSymmetricKeyType(getName(), "AES", 192).description("An 192 bit key suited for AES encryption").add();
        }
    },
    AES_256 {
        public String getName() {
            return "AES 256";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newSymmetricKeyType(getName(), "AES", 256).description("an 256 bit key suited for AES encryption").add();
        }
    },
    TRUSTED_CERTIFICATE {
        public String getName() {
            return "SubCA certificate";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newTrustedCertificateType(getName()).description("Certificate located in a trust store, belongs to a (sub)CA").add();
        }
    },
    PASSWORD {
        public String getName() {
            return "Password";
        }
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService.newPassphraseType(getName()).length(20).withLowerCaseCharacters().withUpperCaseCharacters().description("Generic password").add();
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
                    .description("Client certificates to be used for DLMS/TLS Suite 1, using EC key on curve SECP256R1. This certificate will be linked to a private key.")
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
                    .description("Certificates to be used for DLMS/TLS Suite 2, using EC key on curve SECP384R1. This certificate will be linked to a private key.")
                    .setKeyUsages(EnumSet.of(KeyUsage.keyAgreement, KeyUsage.keyCertSign))
                    .setExtendedKeyUsages(EnumSet.of(ExtendedKeyUsage.tlsWebClientAuthentication, ExtendedKeyUsage.tlsWebServerAuthentication))
                    .ECDSA()
                    .curve("secp384r1")
                    .add();
        }
    },
    GENERAL_PURPOSE_X509_CERTIFICATE {
        @Override
        public String getName() {
            return "X509 Certificate";
        }

        @Override
        public KeyType createKeyType(PkiService pkiService) {
            return pkiService
                    .newCertificateType(getName())
                    .description("General purpose certificate")
                    .add();
        }
    },
    ;

    abstract public String getName();
    abstract public KeyType createKeyType(PkiService pkiService);

}
