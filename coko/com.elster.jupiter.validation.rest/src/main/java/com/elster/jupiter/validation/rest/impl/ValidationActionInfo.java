package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.validation.ValidationAction;

public class ValidationActionInfo {

    public ValidationAction action;

    public ValidationActionInfo(ValidationAction validationAction) {
        action = validationAction;
    }

    public ValidationActionInfo() {
    }
}
