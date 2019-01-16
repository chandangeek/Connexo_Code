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
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The default implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * device command execution
 */
@Component(name = "com.energyict.servicecall.ami.default.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + DefaultDeviceServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class DefaultDeviceServiceCallHandler extends AbstractOperationServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "DefaultDeviceServiceCallHandler";

    public DefaultDeviceServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public DefaultDeviceServiceCallHandler(MessageService messageService, Thesaurus thesaurus, CompletionOptionsCallBack completionOptionsCallBack) {
        super(messageService, thesaurus, completionOptionsCallBack);
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
}