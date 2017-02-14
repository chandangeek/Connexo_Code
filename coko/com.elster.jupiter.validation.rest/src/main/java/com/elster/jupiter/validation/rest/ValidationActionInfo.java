/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationAction;

public class ValidationActionInfo {

    public ValidationAction action;

    public ValidationActionInfo(ValidationAction validationAction) {
        action = validationAction;
    }

    public ValidationActionInfo() {
    }
}
