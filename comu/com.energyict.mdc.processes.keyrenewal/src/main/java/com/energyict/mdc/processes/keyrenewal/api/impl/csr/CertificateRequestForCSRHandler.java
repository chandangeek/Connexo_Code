package com.energyict.mdc.processes.keyrenewal.api.impl.csr;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.cert.X509Certificate;

public class CertificateRequestForCSRHandler implements MessageHandler {

    private final SecurityManagementService securityManagementService;
    private final CaService caService;
    private final TransactionService transactionService;
    private final JsonService jsonService;
    private final ServiceCallService serviceCallService;

    public CertificateRequestForCSRHandler(SecurityManagementService securityManagementService, CaService caService, TransactionService transactionService, JsonService jsonService, ServiceCallService serviceCallService) {
        this.securityManagementService = securityManagementService;
        this.caService = caService;
        this.transactionService = transactionService;
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
    }


    @Override
    public void process(Message message) {
        CertificateRequestForCSRMessage certificateRequestForCSRMessage = jsonService.deserialize(message.getPayload(), CertificateRequestForCSRMessage.class);
        CertificateWrapper certificateWrapper = securityManagementService.findCertificateWrapper(certificateRequestForCSRMessage.alias).get();
        if (!certificateWrapper.hasCSR()) {

        }
        PKCS10CertificationRequest pkcs10CertificationRequest = ((RequestableCertificateWrapper) certificateWrapper).getCSR().get();
        ServiceCall serviceCall = serviceCallService.getServiceCall(certificateRequestForCSRMessage.serviceCall).get();
        executeSignCSR(pkcs10CertificationRequest, certificateWrapper, serviceCall);
    }

    private void executeSignCSR(PKCS10CertificationRequest pkcs10CertificationRequest, CertificateWrapper certificateWrapper, ServiceCall serviceCall) {
        try {
                serviceCall.requestTransition(DefaultState.ONGOING);
                X509Certificate certificate = caService.signCsr(pkcs10CertificationRequest);
                certificateWrapper.setCertificate(certificate);
                certificateWrapper.save();
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } catch (Exception e) {
                serviceCall.requestTransition(DefaultState.FAILED);
        }
    }
}
