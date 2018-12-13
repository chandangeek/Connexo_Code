package com.elster.jupiter.pki;

/**
 * Extra wrappers statuses to be used over X509Certificate statuses
 * <ul>
 * <li>NATIVE - no extra flags, certificate status should be retrieved from X509Certificate object</li>
 * <li>OBSOLETE - certificate is manually marked as obsolete and should not be used anymore</li>
 * <li>REVOKED - certificate is revoked by CA service and can not be used anymore</li>
 * <ul/>
 */
public enum CertificateWrapperStatus {
    NATIVE,
    OBSOLETE,
    REVOKED
}
