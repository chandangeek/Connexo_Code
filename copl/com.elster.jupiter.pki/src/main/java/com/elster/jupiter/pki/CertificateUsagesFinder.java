package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface CertificateUsagesFinder {

    /**
     * Finds associated with the given certificate devices and return names of those
     *
     * @param certificateWrapper certificate to search by
     * @return names list of associated devices
     */
    List<String> findAssociatedDevicesNames(CertificateWrapper certificateWrapper);

    AssociatedDeviceType getAssociatedDeviceType(CertificateWrapper certificateWrapper);

    boolean isCertificateRelatedToType(CertificateWrapper certificateWrapper, String prefix);
}