/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.Thesaurus;

import javax.validation.ConstraintViolationException;

import static java.util.stream.Collectors.toSet;

/**
 * This class wraps the original ConstrainViolationException with one purpose: modify the property path
 **/
public class PathPrependingConstraintViolationException extends ConstraintViolationException {

    public PathPrependingConstraintViolationException(Thesaurus thesaurus, ConstraintViolationException original, String... node) {
        super(original.getMessage(), original.getConstraintViolations().stream().map(cv -> new PathPrependingConstraintViolation<>(thesaurus, cv, node)).collect(toSet()));
    }

}