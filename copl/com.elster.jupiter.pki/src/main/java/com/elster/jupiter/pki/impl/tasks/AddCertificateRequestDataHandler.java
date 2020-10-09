/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.pki.impl.tasks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateRequestData;
import com.elster.jupiter.pki.CertificateType;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionService;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import static com.elster.jupiter.pki.impl.MessageSeeds.ADD_CERTIFICATE_REQUEST_DATA_TASK_FAILED;

public class AddCertificateRequestDataHandler implements TaskExecutor {
    public static final String CA_KEY = "ca";
    public static final String CP_KEY = "cp";
    public static final String EE_KEY = "ee";
    public static final String SUBJECT_DN_FIELDS_KEY = "subjectDNfields";

    private final SecurityManagementService securityManagementService;
    private final ThreadPrincipalService threadPrincipalService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final Logger logger;
    private Map<CertificateType, Map<String, String>> taskParamsMap = new HashMap<>();
    private String path = "";
    private Properties properties;

    public AddCertificateRequestDataHandler(String path, SecurityManagementService securityManagementService, TransactionService transactionService, ThreadPrincipalService threadPrincipalService, Thesaurus thesaurus, Logger logger) {
        this.path = path;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.securityManagementService = securityManagementService;
        this.thesaurus = thesaurus;
        this.logger = logger;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        loadProperties();
        List<CertificateWrapper> certificateWrapperList = this.securityManagementService.findAllCertificates().find();
        for (CertificateWrapper certificateWrapper : certificateWrapperList) {
            for (Map.Entry<CertificateType, Map<String, String>> entry : taskParamsMap.entrySet()) {
                try {
                    if (entry.getKey().isApplicableTo((KeyType) certificateWrapper)) {
                        CertificateRequestData certificateRequestData = new CertificateRequestData(entry.getValue().get(CA_KEY), entry.getValue().get(EE_KEY),
                                entry.getValue().get(CP_KEY), entry.getValue().get(SUBJECT_DN_FIELDS_KEY));
                        logger.info("Updating certificate with id:" + certificateWrapper.getId() + " type:" + entry.getKey().getName());
                        certificateWrapper.setCertificateRequestData(Optional.of(certificateRequestData));
                        logger.info(".... OK");
                        break;
                    }
                } catch (Exception ex) {
                    logger.severe("Exception has occurred for type '" + entry.getKey().getName() + "', certificate id: " + certificateWrapper.getId() + ", error: " + ex.getLocalizedMessage());
                    throw new AddCertificateRequestDataTaskException(thesaurus, ADD_CERTIFICATE_REQUEST_DATA_TASK_FAILED, ex.getLocalizedMessage());
                }
            }
        }
    }

    private void loadProperties() {
        this.properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(path)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setProperty(CertificateType.DIGITAL_SIGNATURE, "DigitalSignature");
        setProperty(CertificateType.KEY_AGREEMENT, "KeyAgreement");
        setProperty(CertificateType.TLS, "TLS");
    }

    private void setProperty(CertificateType certificateType, String prefix) {
        Map<String, String> map = new HashMap<>();
        Object ca = properties.get(prefix + "CertificationAuthorityName");
        if (ca != null && !ca.toString().trim().isEmpty()) {
            map.put(CA_KEY, ca.toString().trim());
        }
        Object cp = properties.get(prefix + "CertificateProfile");
        if (cp != null && !cp.toString().trim().isEmpty()) {
            map.put(CP_KEY, cp.toString().trim());
        }
        Object ee = properties.get(prefix + "EndEntityName");
        if (ee != null && !ee.toString().trim().isEmpty()) {
            map.put(EE_KEY, ee.toString().trim());
        }
        if (map.size() == 3) {
            Object subjectDNfields = properties.get(prefix + "SubjectDNfields");
            if (subjectDNfields != null && !subjectDNfields.toString().trim().isEmpty()) {
                map.put(SUBJECT_DN_FIELDS_KEY, subjectDNfields.toString().trim());
            }
            taskParamsMap.put(certificateType, map);
        }
    }
}
