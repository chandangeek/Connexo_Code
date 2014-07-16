package com.elster.jupiter.validation;

import java.util.List;
import java.util.Map;

public interface ValidatorFactory {

    List<String> available();

    Validator create(String implementation, Map<String, Object> props);

    Validator createTemplate(String implementation);

}
