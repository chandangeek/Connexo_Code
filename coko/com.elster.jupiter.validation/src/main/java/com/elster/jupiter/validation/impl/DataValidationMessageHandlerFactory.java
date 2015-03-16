package com.elster.jupiter.validation.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.validation.ValidationService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;


@Component(name = "com.elster.jupiter.validation.impl",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ValidationServiceImpl.SUBSCRIBER_NAME, "destination="+ ValidationServiceImpl.DESTINATION_NAME},
        immediate = true)
public class DataValidationMessageHandlerFactory implements MessageHandlerFactory {

    private volatile ValidationService validationService;

    @Override
    public MessageHandler newMessageHandler() {
        return new DataValidationMessageHandler(validationService);
    }

    @Reference
    public void setDataExportService(ValidationService validationService) {
        this.validationService = (ValidationService) validationService;
    }

    @Activate
    public void activate(BundleContext context) {
    }

    @Deactivate
    public void deactivate() {

    }

}
