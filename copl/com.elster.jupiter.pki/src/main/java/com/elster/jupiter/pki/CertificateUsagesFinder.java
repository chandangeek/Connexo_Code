package com.elster.jupiter.pki;

import java.util.List;

public interface CertificateUsagesFinder {

    /**
     * Finds associated with the given certificate devices and return names of those
     *
     * @param certificateWrapper certificate to search by
     * @return names list of associated devices
     */
    List<String> findAssociatedDevicesNames(CertificateWrapper certificateWrapper);
}
