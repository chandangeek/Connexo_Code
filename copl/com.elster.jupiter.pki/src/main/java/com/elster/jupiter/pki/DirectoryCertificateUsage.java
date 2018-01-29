/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.util.HasId;

import java.util.Optional;

public interface DirectoryCertificateUsage extends HasId{
    UserDirectory getDirectory();

    Optional<TrustStore> getTrustStore();

    void setTrustStore(TrustStore trustStore);

    Optional<CertificateWrapper> getCertificate();

    void setCertificate(CertificateWrapper certificate);

    void save();

    void delete();
}
