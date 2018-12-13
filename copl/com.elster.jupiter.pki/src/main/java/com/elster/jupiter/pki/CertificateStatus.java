package com.elster.jupiter.pki;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.impl.TranslationKeys;

import java.util.Arrays;
import java.util.Optional;

public enum CertificateStatus {
    AVAILABLE(TranslationKeys.AVAILABLE),
    REQUESTED(TranslationKeys.REQUESTED),
    EXPIRED(TranslationKeys.EXPIRED),
    OBSOLETE(TranslationKeys.OBSOLETE),
    REVOKED(TranslationKeys.REVOKED);

    TranslationKeys statusKeyRef;

    CertificateStatus(TranslationKeys statusRef) {
        this.statusKeyRef = statusRef;
    }

    public String getName() {
        return statusKeyRef.getKey();
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(statusKeyRef).format();
    }

    public static Optional<CertificateStatus> getByTranslationKey(TranslationKey key) {
        return Arrays.stream(values())
                .filter(status -> status.statusKeyRef.equals(key))
                .findFirst();
    }
}
