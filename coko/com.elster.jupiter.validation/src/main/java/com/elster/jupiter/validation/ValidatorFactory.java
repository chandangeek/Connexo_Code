package com.elster.jupiter.validation;

import com.elster.jupiter.util.units.Quantity;

import java.util.List;
import java.util.Map;

public interface ValidatorFactory {

    List<String> available();

    Validator create(String implementation, Map<String, Quantity> props);

}
