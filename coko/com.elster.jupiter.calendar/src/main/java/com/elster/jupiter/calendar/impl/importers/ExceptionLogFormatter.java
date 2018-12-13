/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.impl.MessageSeeds;
import com.elster.jupiter.nls.Thesaurus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by igh on 19/05/2016.
 */
class ExceptionLogFormatter {

    private Logger logger;
    private Thesaurus thesaurus;

    ExceptionLogFormatter(Thesaurus thesaurus, Logger logger) {
        this.thesaurus = thesaurus;
        this.logger = logger;
    }

    public void log(ConstraintViolationException e) {
        for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
            if (constraintViolation.getPropertyPath()!=null) {
                String key = removeCurlyBrace(constraintViolation.getMessageTemplate());
                Optional<MessageSeeds> messageSeeds = Stream.of(MessageSeeds.values()).filter(s -> s.getKey().equals(key)).findFirst();
                //search for message seed
                if (messageSeeds.isPresent()) {
                    messageSeeds.get().log(logger, thesaurus);
                // log without error code
                } else {
                    String typeName = constraintViolation.getRootBeanClass().getAnnotatedInterfaces()[0].getType().getTypeName();
                    int index = typeName.lastIndexOf(".") + 1;
                    if (index != -1) {
                        typeName = typeName.substring(index);
                    }
                    String fieldIdentifier = constraintViolation.getPropertyPath().toString();
                    String fieldLevelMessage = constraintViolation.getMessage();
                    logger.severe(typeName + " " + fieldIdentifier + ": " + fieldLevelMessage);
                }
            }
        }
    }

    private String removeCurlyBrace(String parameter) {
        return parameter.substring(1, parameter.length() - 1);
    }

}
