/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validators.impl.meteradvance.MeterAdvanceValidator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(
        name = "com.elster.jupiter.validators.impl.DefaultValidatorFactory",
        service = {ValidatorFactory.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = "name=" + MessageSeeds.COMPONENT_NAME,
        immediate = true)
public class DefaultValidatorFactory implements ValidatorFactory, MessageSeedProvider, TranslationKeyProvider {

    public static final String THRESHOLD_VALIDATOR = ThresholdValidator.class.getName();
    public static final String MISSING_VALUES_VALIDATOR = MissingValuesValidator.class.getName();
    public static final String REGISTER_INCREASE_VALIDATOR = RegisterIncreaseValidator.class.getName();
    public static final String READING_QUALITIES_VALIDATOR = ReadingQualitiesValidator.class.getName();
    public static final String METER_ADVANCE_VALIDATOR = MeterAdvanceValidator.class.getName();
    public static final String MAIN_CHECK_VALIDATOR = MainCheckValidator.class.getName();
    public static final String REFERENCE_COMPARISON_VALIDATOR = ReferenceComparisonValidator.class.getName();
    public static final String CONSECUTIVE_ZEROS_VALIDATOR = ConsecutiveZerosValidator.class.getName();
    public static final Logger LOGGER = Logger.getLogger(DefaultValidatorFactory.class.getName());

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile ValidationService validationService;
    private volatile MeteringService meteringService;

    public DefaultValidatorFactory() {
    }

    @Inject
    public DefaultValidatorFactory(NlsService nlsService, PropertySpecService propertySpecService,
                                   MetrologyConfigurationService metrologyConfigurationService,
                                   ValidationService validationService, PropertyValueInfoService propertyValueInfoService, MeteringService meteringService) {
        this();
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setMetrologyConfigurationService(metrologyConfigurationService);
        setValidationService(validationService);
        setPropertyValueInfoService(propertyValueInfoService);
        setMeteringService(meteringService);
    }

    @Activate
    public void activate() {
        LOGGER.log(Level.INFO, "Default validation factory activated");
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setValidationService(ValidationService validationService) {
        LOGGER.log(Level.INFO, "Default validation factory: set validation service");
        this.validationService = validationService;
    }

    public void unsetValidationService(ValidationService validationService) {
        LOGGER.log(Level.INFO, "Default validation factory: unset validation service");
        this.validationService = null;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
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
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Override
    public String getComponentName() {
        return MessageSeeds.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        for (ValidatorDefinition validatorDefinition : ValidatorDefinition.values()) {
            IValidator validator = validatorDefinition.createTemplate(new ValidatorParameters(thesaurus, propertySpecService, metrologyConfigurationService, validationService, meteringService));
            translationKeys.add(new SimpleTranslationKey(validator.getNlsKey().getKey(), validator.getDefaultFormat()));
            translationKeys.addAll(validator.getExtraTranslationKeys());
        }
        return translationKeys;
    }

    private enum ValidatorDefinition {
        THRESHOLD(THRESHOLD_VALIDATOR) {
            @Override
            Validator create(ValidatorParameters parameters) {
                return new ThresholdValidator(parameters.thesaurus, parameters.propertySpecService, parameters.props);
            }

            @Override
            IValidator createTemplate(ValidatorParameters parameters) {
                return new ThresholdValidator(parameters.thesaurus, parameters.propertySpecService);
            }
        },
        MISSING_VALUES(MISSING_VALUES_VALIDATOR) {
            @Override
            Validator create(ValidatorParameters parameters) {
                return new MissingValuesValidator(parameters.thesaurus, parameters.propertySpecService);
            }

            @Override
            IValidator createTemplate(ValidatorParameters parameters) {
                return new MissingValuesValidator(parameters.thesaurus, parameters.propertySpecService);
            }
        },
        REGISTER_INCREASE(REGISTER_INCREASE_VALIDATOR) {
            @Override
            Validator create(ValidatorParameters parameters) {
                return new RegisterIncreaseValidator(parameters.thesaurus, parameters.propertySpecService, parameters.props);
            }

            @Override
            IValidator createTemplate(ValidatorParameters parameters) {
                return new RegisterIncreaseValidator(parameters.thesaurus, parameters.propertySpecService);
            }
        },
        READING_QUALITIES(READING_QUALITIES_VALIDATOR) {
            @Override
            Validator create(ValidatorParameters parameters) {
                return new ReadingQualitiesValidator(parameters.thesaurus, parameters.propertySpecService, parameters.props);
            }

            @Override
            IValidator createTemplate(ValidatorParameters parameters) {
                return new ReadingQualitiesValidator(parameters.thesaurus, parameters.propertySpecService);
            }
        },
        MAIN_CHECK(MAIN_CHECK_VALIDATOR) {
            @Override
            Validator create(ValidatorParameters parameters) {
                return new MainCheckValidator(parameters.thesaurus, parameters.propertySpecService, parameters.props, parameters.metrologyConfigurationService, parameters.validationService);
            }

            @Override
            IValidator createTemplate(ValidatorParameters parameters) {
                return new MainCheckValidator(parameters.thesaurus, parameters.propertySpecService, parameters.metrologyConfigurationService, parameters.validationService);
            }
        },
        CONSECUTIVE_ZEROS(CONSECUTIVE_ZEROS_VALIDATOR){
            @Override
            Validator create(ValidatorParameters parameters) {
                return new ConsecutiveZerosValidator(parameters.thesaurus, parameters.propertySpecService, parameters.props);
            }

            @Override
            IValidator createTemplate(ValidatorParameters parameters) {
                return new ConsecutiveZerosValidator(parameters.thesaurus, parameters.propertySpecService);
            }
        },
        METER_ADVANCE(METER_ADVANCE_VALIDATOR) {
            @Override
            Validator create(ValidatorParameters parameters) {
                return new MeterAdvanceValidator(parameters.thesaurus, parameters.propertySpecService, parameters.meteringService, parameters.props);
            }

            @Override
            IValidator createTemplate(ValidatorParameters parameters) {
                return new MeterAdvanceValidator(parameters.thesaurus, parameters.propertySpecService, parameters.meteringService);
            }
        },
        REFERENCE_COMPARISON(REFERENCE_COMPARISON_VALIDATOR) {
            @Override
            Validator create(ValidatorParameters parameters) {
                return new ReferenceComparisonValidator(parameters.thesaurus, parameters.propertySpecService, parameters.metrologyConfigurationService, parameters.validationService, parameters.meteringService, parameters.props);
            }

            @Override
            IValidator createTemplate(ValidatorParameters parameters) {
                return new ReferenceComparisonValidator(parameters.thesaurus, parameters.propertySpecService, parameters.metrologyConfigurationService, parameters.validationService, parameters.meteringService);
            }
        };

        private final String implementation;

        String getImplementation() {
            return implementation;
        }

        ValidatorDefinition(String implementation) {
            this.implementation = implementation;
        }

        abstract Validator create(ValidatorParameters parameters);

        abstract IValidator createTemplate(ValidatorParameters parameters);
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
                return definition.create(new ValidatorParameters(thesaurus, propertySpecService, metrologyConfigurationService, validationService, meteringService, props));
            }
        }
        throw new IllegalArgumentException("Unsupported implementation " + implementation);
    }

    @Override
    public Validator createTemplate(String implementation) {
        for (ValidatorDefinition definition : ValidatorDefinition.values()) {
            if (definition.getImplementation().equals(implementation)) {
                return definition.createTemplate(new ValidatorParameters(thesaurus, propertySpecService, metrologyConfigurationService, validationService, meteringService));
            }
        }
        throw new IllegalArgumentException("Unsupported implementation " + implementation);
    }


    private class ValidatorParameters {
        private Thesaurus thesaurus;
        private PropertySpecService propertySpecService;
        private MetrologyConfigurationService metrologyConfigurationService;
        private ValidationService validationService;
        private MeteringService meteringService;
        private Map<String, Object> props;

        public ValidatorParameters(Thesaurus thesaurus, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, MeteringService meteringService, Map<String, Object> props) {
            this.thesaurus = thesaurus;
            this.propertySpecService = propertySpecService;
            this.metrologyConfigurationService = metrologyConfigurationService;
            this.validationService = validationService;
            this.meteringService = meteringService;
            this.props = props;
        }

        public ValidatorParameters(Thesaurus thesaurus, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, MeteringService meteringService) {
            this(thesaurus, propertySpecService, metrologyConfigurationService, validationService, meteringService, null);
        }
    }
}
