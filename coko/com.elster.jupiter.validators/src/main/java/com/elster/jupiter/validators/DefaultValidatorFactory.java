package com.elster.jupiter.validators;

import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;

@Component(name = "com.elster.jupiter.validators.DefaultValidatorFactory", service = ValidatorFactory.class, immediate=true)
public class DefaultValidatorFactory implements ValidatorFactory {

    static final String RatedPowerValidator = "com.elster.jupiter.validators.RatedPowerValidator";
    static final String MinimalUsageExpectedValidator = "com.elster.jupiter.validators.MinimalUsageExpectedValidator";
    static final String MinMaxValidator = "com.elster.jupiter.validators.MinMaxValidator";
    static final String ConsecutiveZerosValidator = "com.elster.jupiter.validators.ConsecutiveZerosValidator";

    @Override
    public ThreadLocal<List<String>> available() {
        ThreadLocal<List<String>> validators = new ThreadLocal<List<String>>() {
            protected List<String> initialValue() {
                List<String> result = new ArrayList<String>();
                result.add(RatedPowerValidator);
                result.add(MinimalUsageExpectedValidator);
                result.add(MinMaxValidator);
                result.add(ConsecutiveZerosValidator);
                return result;
            }
        };
        return validators;
    }

    @Override
    public Validator create(String implementation) {
        if (implementation.equals(RatedPowerValidator)) {
            return new RatedPowerValidator();
        } else if  (implementation.equals(MinimalUsageExpectedValidator)) {
            return new MinimalUsageExpectedValidator();
        } else if (implementation.equals(MinMaxValidator)) {
            return new MinMaxValidator();
        } else if (implementation.equals(ConsecutiveZerosValidator)) {
            return new ConsecutiveZerosValidator();
        } else {
            return null;
        }
    }
}
