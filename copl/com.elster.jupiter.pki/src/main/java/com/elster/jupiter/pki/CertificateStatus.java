package com.elster.jupiter.pki;

import com.elster.jupiter.pki.impl.TranslationKeys;

public enum CertificateStatus {
    AVAILABLE(TranslationKeys.AVAILABLE),
    REQUESTED(TranslationKeys.REQUESTED),
    EXPIRED(TranslationKeys.EXPIRED),
    OBSOLETE(TranslationKeys.OBSOLETE);

    TranslationKeys statusKeyRef;

    CertificateStatus(TranslationKeys statusRef) {
        this.statusKeyRef = statusRef;
    }

    public String getName(){
        return statusKeyRef.getKey();
    }
}
