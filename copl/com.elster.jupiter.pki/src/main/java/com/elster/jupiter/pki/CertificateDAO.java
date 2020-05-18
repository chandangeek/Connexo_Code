package com.elster.jupiter.pki;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface CertificateDAO {

    List<CertificateWrapper> findExpired(Instant when);

}
