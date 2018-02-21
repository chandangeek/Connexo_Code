/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.util.Registration;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CSRProcessor {
    private final Thesaurus thesaurus;
    private final SecurityManagementService securityManagementService;
    private final CaService caService;

    private final Set<ImportListener> importListeners = new HashSet<>();

    interface ImportListener {
        void created(String alias);
        void updated(String alias);
    }

    @Inject
    public CSRProcessor(SecurityManagementService securityManagementService, CaService caService, Thesaurus thesaurus) {
        this.securityManagementService = securityManagementService;
        this.caService = caService;
        this.thesaurus = thesaurus;
    }

    public Map<String, Map<String, X509Certificate>> process(Map<String, Map<String, PKCS10CertificationRequest>> csrMap) {
        // TODO
        return Collections.emptyMap();
    }

    Registration addListener(ImportListener importListener) {
        importListeners.add(importListener);
        return () -> importListeners.remove(importListener);
    }

}
