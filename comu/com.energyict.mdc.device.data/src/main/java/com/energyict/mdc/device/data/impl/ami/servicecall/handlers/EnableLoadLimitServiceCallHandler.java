package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.data.DeviceDataServices;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * the connect of the devices breaker.
 *
 * @author sva
 * @since 06/06/16 - 13:05
 */
@Component(name = "com.energyict.servicecall.ami.enable.loadlimit.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + EnableLoadLimitServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class EnableLoadLimitServiceCallHandler extends AbstractOperationServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "EnableLoadLimitServiceCallHandler";

    public EnableLoadLimitServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public EnableLoadLimitServiceCallHandler(MessageService messageService, Thesaurus thesaurus) {
        super(messageService, thesaurus);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        super.setMessageService(messageService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        super.setThesaurus(nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN));
    }
}