/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.rest.util;

import javax.validation.ConstraintViolationException;

import static java.util.stream.Collectors.toSet;

/**
 * This class wraps the original ConstrainViolationException with one purpose: modify the property path
 **/
public class PathPrependingConstraintViolationException extends ConstraintViolationException {

    public PathPrependingConstraintViolationException(ConstraintViolationException original, String ... node) {
        super(original.getMessage(), original.getConstraintViolations().stream().map(cv -> new PathPrependingConstraintViolation<>(cv, node)).collect(toSet()));
    }

}