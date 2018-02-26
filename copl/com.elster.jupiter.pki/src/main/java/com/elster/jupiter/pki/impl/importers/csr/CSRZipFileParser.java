/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.nls.Thesaurus;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipInputStream;

class CSRZipFileParser {
    private final Thesaurus thesaurus;

    CSRZipFileParser(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    Map<String, Map<String, PKCS10CertificationRequest>> parseInputStream(InputStream inputStream) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            // TODO
            return Collections.emptyMap();
        }
    }
}
