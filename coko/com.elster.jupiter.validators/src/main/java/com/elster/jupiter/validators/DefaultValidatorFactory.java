package com.elster.jupiter.validators;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.validators.DefaultValidatorFactory", service = ValidatorFactory.class, immediate = true)
public class DefaultValidatorFactory implements ValidatorFactory {

    private volatile Thesaurus thesaurus;

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus("VDR", Layer.DOMAIN);
    }

    private enum ValidatorDefinition {
        RATED_POWER("com.elster.jupiter.validators.RatedPowerValidator") {
            @Override
            Validator create(Thesaurus thesaurus, Map<String, Quantity> props) {
                return new RatedPowerValidator(props);
            }
        },
        MINIMAL_USAGE("com.elster.jupiter.validators.MinimalUsageExpectedValidator") {
            @Override
            Validator create(Thesaurus thesaurus, Map<String, Quantity> props) {
                return new MinimalUsageExpectedValidator(props);
            }
        },
        MIN_MAX("com.elster.jupiter.validators.MinMaxValidator") {
            @Override
            Validator create(Thesaurus thesaurus, Map<String, Quantity> props) {
                return new MinMaxValidator(thesaurus, props);
            }
        },
        CONSECUTIVE_ZEROES("com.elster.jupiter.validators.ConsecutiveZerosValidator") {
            @Override
            Validator create(Thesaurus thesaurus, Map<String, Quantity> props) {
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

        abstract Validator create(Thesaurus thesaurus, Map<String, Quantity> props);
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
                return definition.create(thesaurus, props);
            }
        }
        return null;
    }
}
