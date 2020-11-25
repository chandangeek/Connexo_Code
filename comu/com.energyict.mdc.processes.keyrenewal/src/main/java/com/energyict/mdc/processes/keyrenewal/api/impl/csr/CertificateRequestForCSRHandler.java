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
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.DeviceService;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Logger;

public class CertificateRequestForCSRHandler implements MessageHandler {

    private final SecurityManagementService securityManagementService;
    private final CaService caService;
    private final TransactionService transactionService;
    private final JsonService jsonService;
    private final ServiceCallService serviceCallService;
    private final DeviceService deviceService;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

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
                    .filter(sa -> sa.getKeyAccessorTypeReference()
                            .equals(securityManagementService.findSecurityAccessorTypeByName(certificateRequestForCSRMessage.securityAccessor).get()))
                    .findFirst().get();
            logger.info("Processing certificate request message for device: "
                    + device.getSerialNumber()+" and security accessor: "
                    + securityAccessor.getName()
                    + " - swapped flag:"+securityAccessor.isSwapped());
            String alias = getCertificateType(securityAccessor.getKeyAccessorTypeReference()).getPrefix() + device.getSerialNumber();
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
            logger.info("Signing CSR for "+certificateWrapper.getAlias());
            Optional<CertificateRequestData> certificateRequestData = certificateWrapper.getCertificateRequestData();
            if (!validateCertificateRequestData(certificateRequestData, certificateWrapper.getAlias())){
                // during renewal process the active slot still contains the "old" certificate
                logger.info("Loading certificate request data from active slot");
                Optional actualValue = securityAccessor.getActualPassphraseWrapperReference();
                certificateRequestData = extractAndValidateCertificateRequestData(actualValue);
            }

            if (!certificateRequestData.isPresent()){
                logger.warning("Certificate request data is not available, will fallback to the defaults configured in config.properties!");
            }

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
    private Optional<CertificateRequestData> extractAndValidateCertificateRequestData(Optional slotValue) {
        Optional<CertificateRequestData> certificateRequestData = Optional.empty();

        if (slotValue.isPresent()){
            CertificateWrapper slotCertificate = (CertificateWrapper) slotValue.get();
            certificateRequestData = slotCertificate.getCertificateRequestData();
            logger.info("Found certificate with alias "+slotCertificate.getAlias());
            if (!validateCertificateRequestData(certificateRequestData, slotCertificate.getAlias())){
                logger.warning("Certificate request data is empty on this slot!");
            }
        } else {
            logger.warning("This slot is empty!");
        }

        return certificateRequestData;
    }

    private boolean validateCertificateRequestData(Optional<CertificateRequestData> certificateRequestData, String alias){
        if (certificateRequestData.isPresent()){
            if (certificateRequestData.get().getCertificateProfileName().isEmpty()){
                logger.warning("Certificate request data is present on "+alias+", but not populated!");
            } else {
                logger.info("Certificate request data is populated on "+alias
                        +" with the certificate-profile: " + certificateRequestData.get().getCertificateProfileName());
                return true;
            }
        } else {
            logger.warning("Empty certificateRequestData on "+alias);
        }

        return false;
    }

    private CertificateType getCertificateType(SecurityAccessorType securityAccessorType) {
        return Arrays.stream(CertificateType.values()).filter(ct -> ct.isApplicableTo(securityAccessorType.getKeyType())).findFirst().orElse(CertificateType.OTHER);
    }
}
