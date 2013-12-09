package com.elster.jupiter.validators;

import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.validators.DefaultValidatorFactory", service = ValidatorFactory.class, immediate = true)
public class DefaultValidatorFactory implements ValidatorFactory {

    static final String RatedPowerValidator = "com.elster.jupiter.validators.RatedPowerValidator";
    static final String MinimalUsageExpectedValidator = "com.elster.jupiter.validators.MinimalUsageExpectedValidator";
    static final String MinMaxValidator = "com.elster.jupiter.validators.MinMaxValidator";
    static final String ConsecutiveZerosValidator = "com.elster.jupiter.validators.ConsecutiveZerosValidator";

    private enum ValidatorDefinition {
        RATED_POWER(RatedPowerValidator) {
            @Override
            Validator create(Map<String, Quantity> props) {
                return new RatedPowerValidator(props);
            }
        },
        MINIMAL_USAGE(MinimalUsageExpectedValidator) {
            @Override
            Validator create(Map<String, Quantity> props) {
                return new MinimalUsageExpectedValidator(props);
            }
        },
        MIN_MAX(MinMaxValidator) {
            @Override
            Validator create(Map<String, Quantity> props) {
                return new MinMaxValidator(props);
            }
        },
        CONSECUTIVE_ZEROES(ConsecutiveZerosValidator) {
            @Override
            Validator create(Map<String, Quantity> props) {
                return new ConsecutiveZerosValidator(props);
            }
        };

        private final String implementation;

        String getImplementation() {
            return implementation;
        }

        ValidatorDefinition(String implementation) {
            this.implementation = implementation;
        }

        abstract Validator create(Map<String, Quantity> props);
    }

    @Override
    public List<String> available() {
        List<String> result = new ArrayList<>();
        for (ValidatorDefinition definition : ValidatorDefinition.values()) {
            result.add(definition.getImplementation());
        }
        return result;
    }

    @Override
    public Validator create(String implementation, Map<String, Quantity> props) {
        for (ValidatorDefinition definition : ValidatorDefinition.values()) {
            if (definition.getImplementation().equals(implementation)) {
                return definition.create(props);
            }
        }
        return null;
    }
}
