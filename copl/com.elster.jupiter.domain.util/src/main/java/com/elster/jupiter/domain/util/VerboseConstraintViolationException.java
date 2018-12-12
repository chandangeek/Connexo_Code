/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

public class VerboseConstraintViolationException extends ConstraintViolationException {

	private static final long serialVersionUID = 1L;

	public VerboseConstraintViolationException(Set<? extends ConstraintViolation<?>> constraintViolations) {
        super(buildMessage(constraintViolations), constraintViolations);
    }

    private static String buildMessage(Set<? extends ConstraintViolation<?>> constraintViolations) {
        StringBuilder builder = new StringBuilder();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            builder.append('\n')
                    .append("Constraint violation : ")
                    .append('\n').append('\t')
                    .append("Message : ")
                    .append(constraintViolation.getMessage())
                    .append('\n').append('\t')
                    .append("Class : ")
                    .append(constraintViolation.getLeafBean().getClass().getName())
                    .append('\n').append('\t')
                    .append("Element : ")
                    .append(constraintViolation.getPropertyPath())
                    .append('\n');
        }
        return builder.toString();
    }

}