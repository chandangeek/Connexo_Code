/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl.csr;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.processes.keyrenewal.api.impl.csr.CertificateRequestForCSRHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_TASK_SUBSCRIBER,
                "destination=" + CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_DESTINATION},
        immediate = true)
public class CertificateRequestForCSRHandlerFactory implements MessageHandlerFactory  {
    public static final String CERTIFICATE_REQUEST_FOR_CSR_DESTINATION = "PkrSignCertTopic";
    public static final String CERTIFICATE_REQUEST_FOR_CSR_TASK_SUBSCRIBER = "PkrSignCertSubscriber";
    public static final String CERTIFICATE_REQUEST_FOR_CSR_TASK_SUBSCRIBER_DISPLAYNAME = "Handle request certificate for CSR";

    private volatile SecurityManagementService securityManagementService;
    private volatile CaService caService;
    private volatile TransactionService transactionService;
    private volatile JsonService jsonService;
    private volatile ServiceCallService serviceCallService;
    private volatile DeviceService deviceService;

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setCaService(CaService caService) {
        this.caService = caService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new CertificateRequestForCSRHandler(securityManagementService, caService, transactionService, jsonService, serviceCallService, deviceService);
    }
}
