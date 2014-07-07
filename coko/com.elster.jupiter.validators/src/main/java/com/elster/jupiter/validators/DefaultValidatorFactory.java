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

    static final String COMPONENT_NAME = "VDR";
    private volatile Thesaurus thesaurus;

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    private enum ValidatorDefinition {
        MIN_MAX("com.elster.jupiter.validators.ThresholdValidator") {
            @Override
            Validator create(Thesaurus thesaurus, Map<String, Quantity> props) {
                return new ThresholdValidator(thesaurus, props);
            }

            @Override
            Validator createTemplate() {
                return new ThresholdValidator();
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
        abstract Validator createTemplate();
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

    @Override
    public Validator createTemplate(String implementation) {
        for (ValidatorDefinition definition : ValidatorDefinition.values()) {
            if (definition.getImplementation().equals(implementation)) {
                return definition.createTemplate();
            }
        }
        return null;
    }
}
