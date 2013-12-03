package com.elster.jupiter.validators;

import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.validators.DefaultValidatorFactory", service = ValidatorFactory.class, immediate = true)
public class DefaultValidatorFactory implements ValidatorFactory {

    static final String RatedPowerValidator = "com.elster.jupiter.validators.RatedPowerValidator";
    static final String MinimalUsageExpectedValidator = "com.elster.jupiter.validators.MinimalUsageExpectedValidator";
    static final String MinMaxValidator = "com.elster.jupiter.validators.MinMaxValidator";
    static final String ConsecutiveZerosValidator = "com.elster.jupiter.validators.ConsecutiveZerosValidator";

    @Override
    public List<String> available() {
        List<String> result = new ArrayList<String>();
        result.add(RatedPowerValidator);
        result.add(MinimalUsageExpectedValidator);
        result.add(MinMaxValidator);
        result.add(ConsecutiveZerosValidator);
        return result;
    }

    @Override
    public List<Validator> availableValidators() {
        List<Validator> result = new ArrayList<Validator>();
        result.add(create(RatedPowerValidator, new HashMap()));
        result.add(create(MinimalUsageExpectedValidator, new HashMap()));
        result.add(create(MinMaxValidator, new HashMap()));
        result.add(create(ConsecutiveZerosValidator, new HashMap()));
        return result;
    }



    @Override
    public Validator create(String implementation, Map<String, Quantity> props) {
        if (implementation.equals(RatedPowerValidator)) {
            return new RatedPowerValidator(props);
        } else if (implementation.equals(MinimalUsageExpectedValidator)) {
            return new MinimalUsageExpectedValidator(props);
        } else if (implementation.equals(MinMaxValidator)) {
            return new MinMaxValidator(props);
        } else if (implementation.equals(ConsecutiveZerosValidator)) {
            return new ConsecutiveZerosValidator(props);
        } else {
            return null;
        }
    }
}
