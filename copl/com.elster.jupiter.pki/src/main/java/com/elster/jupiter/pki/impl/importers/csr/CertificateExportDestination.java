package com.elster.jupiter.pki.impl.importers.csr;

import java.io.IOException;

public interface CertificateExportDestination {

    void export(byte[] bytes, byte[] signature) throws IOException;
}
