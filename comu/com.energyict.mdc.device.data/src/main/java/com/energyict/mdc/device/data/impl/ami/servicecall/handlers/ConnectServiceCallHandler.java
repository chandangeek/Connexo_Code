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
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * the connect of the devices breaker.
 *
 * @author sva
 * @since 06/06/16 - 13:05
 */
@Component(name = "com.energyict.servicecall.ami.connect.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + ConnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class ConnectServiceCallHandler extends AbstractContactorOperationServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "ConnectServiceCallHandler";

    public ConnectServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public ConnectServiceCallHandler(MessageService messageService, DeviceService deviceService, Thesaurus thesaurus, CompletionOptionsCallBack completionOptionsCallBack) {
        super.setMessageService(messageService);
        super.setDeviceService(deviceService);
        super.setThesaurus(thesaurus);
        super.setCompletionOptionsCallBack(completionOptionsCallBack);
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
    public void setDeviceService(DeviceService deviceService) {
        super.setDeviceService(deviceService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        super.setThesaurus(nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Override
    protected BreakerStatus getDesiredBreakerStatus() {
        return BreakerStatus.CONNECTED;
    }
}