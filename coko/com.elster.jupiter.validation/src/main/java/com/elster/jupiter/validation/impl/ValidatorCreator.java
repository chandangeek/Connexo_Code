package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.Validator;

import java.util.Map;

public interface ValidatorCreator {

    Validator getValidator(String implementation, Map<String, Object> props);

    Validator getTemplateValidator(String implementation);
}
