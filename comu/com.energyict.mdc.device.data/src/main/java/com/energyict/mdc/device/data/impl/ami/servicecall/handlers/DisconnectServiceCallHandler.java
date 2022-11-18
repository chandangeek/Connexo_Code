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
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.upl.meterdata.BreakerStatus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * the connect of the devices breaker.
 *
 * @author sva
 * @since 06/06/16 - 13:05
 */
@Component(name = "com.energyict.servicecall.ami.disconnect.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + DisconnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class DisconnectServiceCallHandler extends AbstractContactorOperationServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "DisconnectServiceCallHandler";

    public DisconnectServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public DisconnectServiceCallHandler(MessageService messageService, DeviceService deviceService, Thesaurus thesaurus, CompletionOptionsCallBack completionOptionsCallBack,
                                        ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService,
                                        EngineConfigurationService engineConfigurationService, PriorityComTaskService priorityComTaskService,
                                        DeviceMessageService deviceMessageService) {
        super.setMessageService(messageService);
        super.setDeviceService(deviceService);
        super.setThesaurus(thesaurus);
        super.setCompletionOptionsCallBack(completionOptionsCallBack);
        super.setDeviceMessageService(deviceMessageService);
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

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        super.setDeviceMessageService(deviceMessageService);
    }

    @Override
    protected BreakerStatus getDesiredBreakerStatus() {
        return BreakerStatus.DISCONNECTED;
    }
}
