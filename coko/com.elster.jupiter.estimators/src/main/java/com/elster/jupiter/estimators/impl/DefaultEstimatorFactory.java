package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimators.MessageSeeds;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpecService;

import com.elster.jupiter.validation.ValidationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.estimators.impl.DefaultEstimatorFactory", service = {EstimatorFactory.class, InstallService.class}, property = "name=" + MessageSeeds.COMPONENT_NAME, immediate = true)
public class DefaultEstimatorFactory implements EstimatorFactory, InstallService {

    public static final String VALUE_FILL_ESTIMATOR = ValueFillEstimator.class.getName();
    public static final String LINEAR_INTERPOLATION_ESTIMATOR = LinearInterpolation.class.getName();
    public static final String AVG_WITH_SAMPLES_ESTIMATOR = AverageWithSamplesEstimator.class.getName();

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile ValidationService validationService;

    public DefaultEstimatorFactory() {
	}

    @Inject
    public DefaultEstimatorFactory(NlsService nlsService, PropertySpecService propertySpecService, ValidationService validationService) {
    	setNlsService(nlsService);
    	setPropertySpecService(propertySpecService);
        setValidationService(validationService);
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
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void install() {
        List<Translation> translations = new ArrayList<>(EstimatorDefinition.values().length);
        for (EstimatorDefinition estimatorDefinition : EstimatorDefinition.values()) {
            IEstimator estimator = estimatorDefinition.createTemplate(thesaurus, propertySpecService, validationService);
            Translation translation = SimpleTranslation.translation(estimator.getNlsKey(), Locale.ENGLISH, estimator.getDefaultFormat());
            translations.add(translation);
            estimator.getPropertySpecs()
                    .stream()
                    .map(key -> SimpleTranslation.translation(estimator.getPropertyNlsKey(key.getName()), Locale.ENGLISH, estimator.getPropertyDefaultFormat(key.getName())))
                    .forEach(translations::add);
            estimator.getExtraTranslations()
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

    private enum EstimatorDefinition {
        VALUE_FILL(VALUE_FILL_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, Map<String, Object> props) {
                return new ValueFillEstimator(thesaurus, propertySpecService, props);
            }

            @Override
            IEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService) {
                return new ValueFillEstimator(thesaurus, propertySpecService);
            }
        },
        LINEAR_INTERPOLATION(LINEAR_INTERPOLATION_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, Map<String, Object> props) {
                return new LinearInterpolation(thesaurus, propertySpecService, props);
            }

            @Override
            IEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService) {
                return new LinearInterpolation(thesaurus, propertySpecService);
            }
        },
        AVG_WITH_SAMPLES(AVG_WITH_SAMPLES_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, Map<String, Object> props) {
                return new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, props);
            }

            @Override
            IEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService) {
                return new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService);
            }
        };


        private final String implementation;

        String getImplementation() {
            return implementation;
        }

        EstimatorDefinition(String implementation) {
            this.implementation = implementation;
        }

        abstract Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, Map<String, Object> props);

        abstract IEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService);
    }

    @Override
    public List<String> available() {
        List<String> result = new ArrayList<>();
        for (EstimatorDefinition definition : EstimatorDefinition.values()) {
            result.add(definition.getImplementation());
        }
        return result;
    }

    @Override
    public Estimator create(String implementation, Map<String, Object> props) {
        for (EstimatorDefinition definition : EstimatorDefinition.values()) {
            if (definition.getImplementation().equals(implementation)) {
                return definition.create(thesaurus, propertySpecService, validationService, props);
            }
        }
        throw new IllegalArgumentException("Unsupported implementation " + implementation);
    }

    @Override
    public Estimator createTemplate(String implementation) {
        for (EstimatorDefinition definition : EstimatorDefinition.values()) {
            if (definition.getImplementation().equals(implementation)) {
                return definition.createTemplate(thesaurus, propertySpecService, validationService);
            }
        }
        throw new IllegalArgumentException("Unsupported implementation " + implementation);
    }
}
