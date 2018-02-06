package com.elster.jupiter.pki;

import java.util.Optional;

/**
 * Extra wrappers statuses to be used over X509Certificate statuses
 * <ul>
 * <li>NATIVE - no extra flags, certificate status should be retrieved from X509Certificate object</li>
 * <li>OBSOLETE - certificate is manually marked as obsolete and should not be used anymore</li>
 * <li>REVOKED - certificate is revoked by CA service and can not be used anymore</li>
 * <ul/>
 */
public enum CertificateWrapperStatus {

    NATIVE(0),
    OBSOLETE(1),
    REVOKED(2);

    long statusDBKey;

    CertificateWrapperStatus(int statusDBKey) {
        this.statusDBKey = statusDBKey;
    }

    public static Optional<CertificateWrapperStatus> fromDbKey(long statusDBKey){
        for (CertificateWrapperStatus st : values()){
            if(st.statusDBKey == statusDBKey){
                return Optional.of(st);
            }
        }
        return Optional.empty();
    }

    public long statusDBKey() {
        return statusDBKey;
    }
}
