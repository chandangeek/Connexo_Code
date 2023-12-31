/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * request csr
 */
@Component(name = "com.energyict.servicecall.ami.generate.csr.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + GenerateCSRServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class GenerateCSRServiceCallHandler extends AbstractOperationServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "DeviceGenerateCsrServiceCallHandler";

    public GenerateCSRServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public GenerateCSRServiceCallHandler(MessageService messageService, Thesaurus thesaurus, CompletionOptionsCallBack completionOptionsCallBack,
                                         DeviceMessageService deviceMessageService) {
        super(messageService, thesaurus, completionOptionsCallBack, deviceMessageService);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        super.setMessageService(messageService);
    }

    @Reference
    protected void setCompletionOptionsCallBack(CompletionOptionsCallBack completionOptionsCallBack) {
        super.setCompletionOptionsCallBack(completionOptionsCallBack);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        super.setThesaurus(nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        super.setDeviceMessageService(deviceMessageService);
    }
}