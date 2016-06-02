package com.elster.jupiter.calendar.importers.impl;


import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by igh on 19/05/2016.
 */
public class ExceptionLogFormatter {

    private Logger logger;
    private Thesaurus thesaurus;

    public ExceptionLogFormatter(Thesaurus thesaurus, Logger logger) {
        this.thesaurus = thesaurus;
        this.logger = logger;
    }

    public void log(ConstraintViolationException e) {
        for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
            if (constraintViolation.getPropertyPath()!=null) {
                String key = removeCurlyBrace(constraintViolation.getMessageTemplate());
                Optional<MessageSeeds> messageSeeds = Arrays.asList(MessageSeeds.values()).stream().filter(s -> s.getKey().equals(key)).findFirst();
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
