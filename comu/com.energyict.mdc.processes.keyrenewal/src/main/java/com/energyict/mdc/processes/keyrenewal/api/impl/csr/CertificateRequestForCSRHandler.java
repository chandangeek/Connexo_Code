/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl.csr;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateRequestData;
import com.elster.jupiter.pki.CertificateType;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class CertificateRequestForCSRHandler implements MessageHandler {

    private final SecurityManagementService securityManagementService;
    private final CaService caService;
    private final TransactionService transactionService;
    private final JsonService jsonService;
    private final ServiceCallService serviceCallService;
    private final DeviceService deviceService;

    public CertificateRequestForCSRHandler(SecurityManagementService securityManagementService, CaService caService, TransactionService transactionService, JsonService jsonService, ServiceCallService serviceCallService, DeviceService deviceService) {
        this.securityManagementService = securityManagementService;
        this.caService = caService;
        this.transactionService = transactionService;
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
        this.deviceService = deviceService;
    }

    @Override
    public void process(Message message) {
        CertificateRequestForCSRMessage certificateRequestForCSRMessage = jsonService.deserialize(message.getPayload(), CertificateRequestForCSRMessage.class);
        ServiceCall serviceCall = serviceCallService.getServiceCall(certificateRequestForCSRMessage.serviceCall).get();
        try {

            ClientCertificateWrapper certificateWrapper;//securityManagementService.findCertificateWrapper(certificateRequestForCSRMessage.alias).get();
            Device device = deviceService.findDeviceById(certificateRequestForCSRMessage.device).get();
            SecurityAccessor securityAccessor = device.getSecurityAccessors().stream()
                    .filter(sa -> sa.getKeyAccessorType()
                            .equals(securityManagementService.findSecurityAccessorTypeByName(certificateRequestForCSRMessage.securityAccessor).get()))
                    .findFirst().get();
            String alias = getCertificateType(securityAccessor.getKeyAccessorType()).getPrefix() + device.getSerialNumber();
            certificateWrapper = securityManagementService
                    .findCertificateWrappers(Where.where("alias").like("*-" + alias))
                    .stream()
                    .filter(cw -> cw instanceof ClientCertificateWrapper)
                    .map(ClientCertificateWrapper.class::cast)
                    .max(Comparator.comparing(cw -> getSequentialNumber(cw, alias)))
                    .orElseThrow(IllegalStateException::new);

            PKCS10CertificationRequest pkcs10CertificationRequest = certificateWrapper.getCSR().get();
            if (securityAccessor instanceof CertificateAccessor) {
                executeSignCSR(pkcs10CertificationRequest, certificateWrapper, securityAccessor, serviceCall);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    private Integer getSequentialNumber(CertificateWrapper certificateWrapper, String alias) {
        try {
            return Integer.valueOf(certificateWrapper.getAlias().replace("-" + alias, ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void executeSignCSR(PKCS10CertificationRequest pkcs10CertificationRequest, CertificateWrapper certificateWrapper, SecurityAccessor securityAccessor, ServiceCall serviceCall) {
        try {
            if(serviceCall.canTransitionTo(DefaultState.ONGOING)) {
                serviceCall.requestTransition(DefaultState.ONGOING);
            }

            // assuming this is called during renew and therefore certificateWrapper received as param is the old one and already populated
            Optional<CertificateRequestData> certificateRequestData = certificateWrapper.getCertificateRequestData();
            X509Certificate certificate = caService.signCsr(pkcs10CertificationRequest, certificateRequestData);
            certificateWrapper.setCertificate(certificate, certificateRequestData);
            certificateWrapper.save();

            securityAccessor.setTempValue(certificateWrapper);
            securityAccessor.save();
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } catch (Exception e) {
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    private CertificateType getCertificateType(SecurityAccessorType securityAccessorType) {
        return Arrays.stream(CertificateType.values()).filter(ct -> ct.isApplicableTo(securityAccessorType.getKeyType())).findFirst().orElse(CertificateType.OTHER);
    }
}
