package com.elster.jupiter.validation.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.validation.ValidationService;


public class DataValidationMessageHandler implements MessageHandler {

    private final ValidationService validationService;

    public DataValidationMessageHandler(ValidationService validationService) {
        this.validationService = (ValidationService) validationService;
    }



    @Override
    public void process(Message message) {

        //impl

    }
}
