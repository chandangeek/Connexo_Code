package com.elster.jupiter.pki.impl.importers.csr;

import java.io.IOException;
import java.util.Optional;

public interface CertificateExportDestination {
    void export(byte[] bytes, byte[] signature) throws IOException;

    Optional<String> getLastExportedFileName();

    String getUrl();
}
