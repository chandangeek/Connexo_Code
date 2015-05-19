package com.elster.jupiter.validation;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;
import java.util.Map;

@ConsumerType
public interface ValidatorFactory {

    List<String> available();

    Validator create(String implementation, Map<String, Object> props);

    Validator createTemplate(String implementation);

}
