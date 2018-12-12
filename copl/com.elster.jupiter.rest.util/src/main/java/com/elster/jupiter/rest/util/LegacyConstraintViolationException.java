/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.validation.ConstraintViolationException;
import java.util.Map;

import static java.util.stream.Collectors.toSet;

/**
 * This class wraps the original ConstrainViolationException with one purpose: catch and remap fields
 * whose name changed, to the original field name, for reasons of backwards compatibility.
 **/
public class LegacyConstraintViolationException extends ConstraintViolationException {

    public LegacyConstraintViolationException(ConstraintViolationException original, Map<String, String> propertyRenames) {
        super(original.getMessage(), original.getConstraintViolations().stream().map(cv -> new LegacyConstraintViolation<>(cv, propertyRenames)).collect(toSet()));
    }

}