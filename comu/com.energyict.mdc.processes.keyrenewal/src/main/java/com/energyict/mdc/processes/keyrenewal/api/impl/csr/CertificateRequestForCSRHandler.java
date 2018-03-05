package com.energyict.mdc.processes.keyrenewal.api.impl.csr;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceCSR;
import com.energyict.mdc.device.data.DeviceService;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.cert.X509Certificate;
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
            Optional<DeviceCSR> collectedDeviceCSR = deviceService.getDeviceCSR(device);
            if (collectedDeviceCSR.isPresent()) {
                certificateWrapper = securityManagementService.findClientCertificateWrapper(certificateRequestForCSRMessage.alias).orElseGet(() ->
                        securityManagementService.newClientCertificateWrapper(securityAccessor.getKeyAccessorType().getKeyType(), securityAccessor.getKeyAccessorType()
                                .getKeyEncryptionMethod())
                                .alias(device.getSerialNumber())
                                .add());
                certificateWrapper.setCSR(collectedDeviceCSR.get().getCSR().get(), securityAccessor.getKeyAccessorType()
                        .getKeyType()
                        .getKeyUsages(), securityAccessor.getKeyAccessorType().getKeyType().getExtendedKeyUsages());
                certificateWrapper.save();
            } else {
                certificateWrapper = securityManagementService.findClientCertificateWrapper(certificateRequestForCSRMessage.alias).orElseThrow(IllegalStateException::new);
            }

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

    private void executeSignCSR(PKCS10CertificationRequest pkcs10CertificationRequest, CertificateWrapper certificateWrapper, SecurityAccessor securityAccessor, ServiceCall serviceCall) {
        try {
            serviceCall.requestTransition(DefaultState.ONGOING);
            X509Certificate certificate = caService.signCsr(pkcs10CertificationRequest);
            certificateWrapper.setCertificate(certificate);
            certificateWrapper.save();

            securityAccessor.setTempValue(certificateWrapper);
            securityAccessor.save();
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } catch (Exception e) {
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }
}
