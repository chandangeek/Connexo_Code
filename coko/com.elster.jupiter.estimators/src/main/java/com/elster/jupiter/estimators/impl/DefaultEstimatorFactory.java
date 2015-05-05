package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.estimators.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.exception.ExceptionCatcher;
import com.elster.jupiter.validation.ValidationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.estimators.impl.DefaultEstimatorFactory", service = {EstimatorFactory.class, InstallService.class}, property = "name=" + MessageSeeds.COMPONENT_NAME, immediate = true)
public class DefaultEstimatorFactory implements EstimatorFactory, InstallService {

    public static final String VALUE_FILL_ESTIMATOR = ValueFillEstimator.class.getName();
    public static final String LINEAR_INTERPOLATION_ESTIMATOR = LinearInterpolation.class.getName();
    public static final String AVG_WITH_SAMPLES_ESTIMATOR = AverageWithSamplesEstimator.class.getName();
    public static final String POWER_GAP_FILL_ESTIMATOR = PowerGapFill.class.getName();

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile ValidationService validationService;
    private volatile MeteringService meteringService;

    public DefaultEstimatorFactory() {
    }

    @Inject
    public DefaultEstimatorFactory(NlsService nlsService, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService) {
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setValidationService(validationService);
        setMeteringService(meteringService);
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

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void install() {
        ExceptionCatcher.executing(() -> {
            List<Translation> translations = new ArrayList<>();
            for (EstimatorDefinition estimatorDefinition : EstimatorDefinition.values()) {
                AbstractEstimator estimator = estimatorDefinition.createTemplate(thesaurus, propertySpecService, validationService, meteringService);
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
        }).andHandleExceptionsWith(Throwable::printStackTrace)
          .execute();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS");
    }

    private enum EstimatorDefinition {
        VALUE_FILL(VALUE_FILL_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, Map<String, Object> props) {
                return new ValueFillEstimator(thesaurus, propertySpecService, props);
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService) {
                return new ValueFillEstimator(thesaurus, propertySpecService);
            }
        },
        LINEAR_INTERPOLATION(LINEAR_INTERPOLATION_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, Map<String, Object> props) {
                return new LinearInterpolation(thesaurus, propertySpecService, props);
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService) {
                return new LinearInterpolation(thesaurus, propertySpecService);
            }
        },
        AVG_WITH_SAMPLES(AVG_WITH_SAMPLES_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, Map<String, Object> props) {
                return new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, props);
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService) {
                return new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService);
            }
        },
        POWER_GAP_FILL(POWER_GAP_FILL_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, Map<String, Object> props) {
                return new PowerGapFill(thesaurus, propertySpecService, props);
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService) {
                return new PowerGapFill(thesaurus, propertySpecService);
            }
        };

        private final String implementation;

        String getImplementation() {
            return implementation;
        }

        EstimatorDefinition(String implementation) {
            this.implementation = implementation;
        }

        abstract Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, Map<String, Object> props);

        abstract AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService);

        public boolean matches(String implementation) {
            return this.implementation.equals(implementation);
        }
    }

    @Override
    public List<String> available() {
        return estimatorDefinitions()
                .map(EstimatorDefinition::getImplementation)
                .collect(Collectors.toList());
    }

    private Stream<EstimatorDefinition> estimatorDefinitions() {
        return Arrays.stream(EstimatorDefinition.values());
    }

    @Override
    public Estimator create(String implementation, Map<String, Object> props) {
        return estimatorDefinitions()
                .filter(estimatorDefinition -> estimatorDefinition.matches(implementation))
                .findFirst()
                .map(estimatorDefinition -> estimatorDefinition.create(thesaurus, propertySpecService, validationService, meteringService, props))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported implementation " + implementation));
    }

    @Override
    public Estimator createTemplate(String implementation) {
        return estimatorDefinitions()
                .filter(estimatorDefinition -> estimatorDefinition.matches(implementation))
                .findFirst()
                .map(estimatorDefinition -> estimatorDefinition.createTemplate(thesaurus, propertySpecService, validationService, meteringService))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported implementation " + implementation));
    }
}
