package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validators.MessageSeeds;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.validators.impl.DefaultValidatorFactory", service = {ValidatorFactory.class, InstallService.class}, property = "name=" + MessageSeeds.COMPONENT_NAME, immediate = true)
public class DefaultValidatorFactory implements ValidatorFactory, InstallService {

    public static final String THRESHOLD_VALIDATOR = ThresholdValidator.class.getName();
    public static final String MISSING_VALUES_VALIDATOR = MissingValuesValidator.class.getName();
    public static final String REGISTER_INCREASE_VALIDATOR = RegisterIncreaseValidator.class.getName();
    public static final String INTERVAL_STATE_VALIDATOR = IntervalStateValidator.class.getName();

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;

    public DefaultValidatorFactory() {
	}

    @Inject
    public DefaultValidatorFactory(NlsService nlsService, PropertySpecService propertySpecService, MeteringService meteringService) {
    	setNlsService(nlsService);
    	setPropertySpecService(propertySpecService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void install() {
        List<Translation> translations = new ArrayList<>(ValidatorDefinition.values().length);
        for (ValidatorDefinition validatorDefinition : ValidatorDefinition.values()) {
            IValidator validator = validatorDefinition.createTemplate(thesaurus, propertySpecService, meteringService);
            Translation translation = SimpleTranslation.translation(validator.getNlsKey(), Locale.ENGLISH, validator.getDefaultFormat());
            translations.add(translation);
            validator.getPropertySpecs()
                    .stream()
                    .map(key -> SimpleTranslation.translation(validator.getPropertyNlsKey(key.getName()), Locale.ENGLISH, validator.getPropertyDefaultFormat(key.getName())))
                    .forEach(translations::add);
            validator.getExtraTranslations()
                    .stream()
                    .map(extraTranslation -> SimpleTranslation.translation(extraTranslation.getFirst(), Locale.ENGLISH, extraTranslation.getLast()))
                    .forEach(translations::add);
        }
        thesaurus.addTranslations(translations);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS");
    }

    private enum ValidatorDefinition {
        THRESHOLD(THRESHOLD_VALIDATOR) {
            @Override
            Validator create(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> props) {
                return new ThresholdValidator(thesaurus, propertySpecService, props);
            }

            @Override
            IValidator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService) {
                return new ThresholdValidator(thesaurus, propertySpecService);
            }
        },
        MISSING_VALUES(MISSING_VALUES_VALIDATOR) {
            @Override
            Validator create(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> props) {
                return new MissingValuesValidator(thesaurus, propertySpecService);
            }

            @Override
            IValidator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService) {
                return new MissingValuesValidator(thesaurus, propertySpecService);
            }
        },
        REGISTER_INCREASE(REGISTER_INCREASE_VALIDATOR) {
            @Override
            Validator create(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> props) {
                return new RegisterIncreaseValidator(thesaurus, propertySpecService, props);
            }

            @Override
            IValidator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService) {
                return new RegisterIncreaseValidator(thesaurus, propertySpecService);
            }
        },
        INTERVAL_STATE(INTERVAL_STATE_VALIDATOR) {
            @Override
            Validator create(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> props) {
                return new IntervalStateValidator(thesaurus, propertySpecService, props);
            }

            @Override
            IValidator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService) {
                return new IntervalStateValidator(thesaurus, propertySpecService);
            }
        };

        private final String implementation;

        String getImplementation() {
            return implementation;
        }

        ValidatorDefinition(String implementation) {
            this.implementation = implementation;
        }

        abstract Validator create(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> props);

        abstract IValidator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService);
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
    public Validator create(String implementation, Map<String, Object> props) {
        for (ValidatorDefinition definition : ValidatorDefinition.values()) {
            if (definition.getImplementation().equals(implementation)) {
                return definition.create(thesaurus, propertySpecService, meteringService, props);
            }
        }
        throw new IllegalArgumentException("Unsupported implementation " + implementation);
    }

    @Override
    public Validator createTemplate(String implementation) {
        for (ValidatorDefinition definition : ValidatorDefinition.values()) {
            if (definition.getImplementation().equals(implementation)) {
                return definition.createTemplate(thesaurus, propertySpecService, meteringService);
            }
        }
        throw new IllegalArgumentException("Unsupported implementation " + implementation);
    }
}
