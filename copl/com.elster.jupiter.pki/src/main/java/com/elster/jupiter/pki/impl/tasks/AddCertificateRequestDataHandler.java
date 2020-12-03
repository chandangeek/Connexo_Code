/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.pki.impl.tasks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.AssociatedDeviceType;
import com.elster.jupiter.pki.CertificateRequestData;
import com.elster.jupiter.pki.CertificateType;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.impl.SecurityManagementServiceImpl;
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

import static com.elster.jupiter.pki.CertificateType.DIGITAL_SIGNATURE;
import static com.elster.jupiter.pki.CertificateType.TLS;
import static com.elster.jupiter.pki.CertificateType.WEBTLS;
import static com.elster.jupiter.pki.impl.MessageSeeds.ADD_CERTIFICATE_REQUEST_DATA_TASK_FAILED;

public class AddCertificateRequestDataHandler implements TaskExecutor {
    public static final String CA_KEY = "ca";
    public static final String CP_KEY = "cp";
    public static final String EE_KEY = "ee";

    private final SecurityManagementServiceImpl securityManagementServiceImpl;
    private final ThreadPrincipalService threadPrincipalService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final Logger logger;
    private Map<CertificateType, Map<String, String>> taskBeaconParamsMap = new HashMap<>();
    private Map<CertificateType, Map<String, String>> taskMeterParamsMap = new HashMap<>();
    private String dNfields = "";
    private String path = "";
    private Properties properties;

    public AddCertificateRequestDataHandler(String path, SecurityManagementServiceImpl securityManagementServiceImpl, TransactionService transactionService, ThreadPrincipalService threadPrincipalService, Thesaurus thesaurus, Logger logger) {
        this.path = path;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.securityManagementServiceImpl = securityManagementServiceImpl;
        this.thesaurus = thesaurus;
        this.logger = logger;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        loadProperties();
        List<CertificateWrapper> certificateWrapperList = this.securityManagementServiceImpl.findAllCertificates().find();
        for (CertificateWrapper certificateWrapper : certificateWrapperList) {
            for (Map.Entry<CertificateType, Map<String, String>> entry : taskBeaconParamsMap.entrySet()) {
                try {
                    if (entry.getKey().isApplicableTo(certificateWrapper)) {
                        switch (entry.getKey()) {
                            case DIGITAL_SIGNATURE:
                                if (securityManagementServiceImpl.isCertificateAssociatedWithDeviceType(certificateWrapper, AssociatedDeviceType.METER)) {
                                    updateCertificateWrapper(taskMeterParamsMap.get(DIGITAL_SIGNATURE), certificateWrapper, entry.getKey().getName());
                                } else if (securityManagementServiceImpl.isCertificateAssociatedWithDeviceType(certificateWrapper, AssociatedDeviceType.BEACON)) {
                                    updateCertificateWrapper(entry.getValue(), certificateWrapper, entry.getKey().getName());
                                }
                                break;
                            case TLS:
                            case WEBTLS:
                                if (securityManagementServiceImpl.isCertificateRelatedToType(certificateWrapper, TLS.getPrefix().substring(0, TLS.getPrefix().length()-1))) {
                                    updateCertificateWrapper(taskBeaconParamsMap.get(TLS), certificateWrapper, TLS.getName());
                                } else if (securityManagementServiceImpl.isCertificateRelatedToType(certificateWrapper, WEBTLS.getPrefix().substring(0, WEBTLS.getPrefix().length()-1))) {
                                    updateCertificateWrapper(taskBeaconParamsMap.get(WEBTLS), certificateWrapper, WEBTLS.getName());
                                }
                                break;
                            case KEY_AGREEMENT:
                                updateCertificateWrapper(entry.getValue(), certificateWrapper, entry.getKey().getName());
                                break;
                        }
                        break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.severe("Exception has occurred for type '" + entry.getKey().getName() + "', certificate id: " + certificateWrapper.getId() + ", error: " + ex.getLocalizedMessage());
                    throw new AddCertificateRequestDataTaskException(thesaurus, ADD_CERTIFICATE_REQUEST_DATA_TASK_FAILED, ex.getLocalizedMessage());
                }
            }
        }
    }

    private void updateCertificateWrapper(Map<String, String> properties, CertificateWrapper certificateWrapper, String certificateTypeName) {
        if (properties != null && !properties.isEmpty()) {
            CertificateRequestData certificateRequestData = new CertificateRequestData(properties.get(CA_KEY), properties.get(EE_KEY),
                    properties.get(CP_KEY), dNfields);
            logger.info("Updating certificate with id:" + certificateWrapper.getId() + " type:" + certificateTypeName);
            certificateWrapper.setCertificateRequestData(Optional.of(certificateRequestData));
            logger.info(".... OK");
        }
    }

    private void loadProperties() {
        this.properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(path)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setProperty(taskBeaconParamsMap, DIGITAL_SIGNATURE, "BeaconDigitalSignature");
        setProperty(taskBeaconParamsMap, CertificateType.KEY_AGREEMENT, "BeaconKeyAgreement");
        setProperty(taskBeaconParamsMap, CertificateType.TLS, "BeaconDLMSTLS");
        setProperty(taskBeaconParamsMap, CertificateType.WEBTLS, "BeaconTLS");
        setProperty(taskMeterParamsMap, DIGITAL_SIGNATURE, "MeterDigitalSignature");

        Object subjectDNfields = properties.get("SubjectDNfields");
        if (subjectDNfields != null && !subjectDNfields.toString().trim().isEmpty()) {
            dNfields = subjectDNfields.toString().trim();
        }
    }

    private void setProperty(Map<CertificateType, Map<String, String>> mapProperty, CertificateType certificateType, String prefix) {
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
            mapProperty.put(certificateType, map);
        }
    }
}
