/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
        name = "com.elster.jupiter.estimators.impl.DefaultEstimatorFactory",
        service = {EstimatorFactory.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = "name=" + MessageSeeds.COMPONENT_NAME,
        immediate = true)
public class DefaultEstimatorFactory implements EstimatorFactory, TranslationKeyProvider, MessageSeedProvider {

    public static final String VALUE_FILL_ESTIMATOR = ValueFillEstimator.class.getName();
    public static final String LINEAR_INTERPOLATION_ESTIMATOR = LinearInterpolation.class.getName();
    public static final String AVG_WITH_SAMPLES_ESTIMATOR = AverageWithSamplesEstimator.class.getName();
    public static final String POWER_GAP_FILL_ESTIMATOR = PowerGapFill.class.getName();
    public static final String EQUAL_DISTRIBUTION_ESTIMATOR = EqualDistribution.class.getName();
    public static final String NEAREST_AVERAGE_VALUE_DAY_ESTIMATOR = NearestAvgValueDayEstimator.class.getName();

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile ValidationService validationService;
    private volatile MeteringService meteringService;
    private volatile TimeService timeService;
    private volatile CalendarService calendarService;

    public DefaultEstimatorFactory() {
    }

    @Inject
    public DefaultEstimatorFactory(NlsService nlsService, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService) {
        this();
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setValidationService(validationService);
        setMeteringService(meteringService);
        setTimeService(timeService);
        setCalendarService(calendarService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN)
                .join(nlsService.getThesaurus(EstimationService.COMPONENTNAME, Layer.DOMAIN));
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

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {this.calendarService = calendarService; }

    @Override
    public String getComponentName() {
        // Translation keys from estimators are historically shared with estimation bundle, most likely due to the fact
        // that estimation thesaurus is asked for these translations somewhere outside of this bundle,
        // so need to persist them with estimation component name for the time being. To be corrected in the future.
        return EstimationService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Collections.addAll(keys, AverageWithSamplesEstimator.TranslationKeys.values());
        Collections.addAll(keys, EqualDistribution.TranslationKeys.values());
        Collections.addAll(keys, LinearInterpolation.TranslationKeys.values());
        Collections.addAll(keys, PowerGapFill.TranslationKeys.values());
        Collections.addAll(keys, ValueFillEstimator.TranslationKeys.values());
        Collections.addAll(keys, NearestAvgValueDayEstimator.TranslationKeys.values());
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    private enum EstimatorDefinition {
        VALUE_FILL(VALUE_FILL_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService, Map<String, Object> props) {
                return new ValueFillEstimator(thesaurus, propertySpecService, props);
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService) {
                return new ValueFillEstimator(thesaurus, propertySpecService);
            }
        },
        LINEAR_INTERPOLATION(LINEAR_INTERPOLATION_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService, Map<String, Object> props) {
                return new LinearInterpolation(thesaurus, propertySpecService, props);
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService) {
                return new LinearInterpolation(thesaurus, propertySpecService);
            }
        },
        AVG_WITH_SAMPLES(AVG_WITH_SAMPLES_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService, Map<String, Object> props) {
                return new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService) {
                return new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService);
            }
        },
        POWER_GAP_FILL(POWER_GAP_FILL_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService, Map<String, Object> props) {
                return new PowerGapFill(thesaurus, propertySpecService, props);
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService) {
                return new PowerGapFill(thesaurus, propertySpecService);
            }
        },
        EQUAL_DISTRIBUTION(EQUAL_DISTRIBUTION_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService, Map<String, Object> props) {
                return new EqualDistribution(thesaurus, propertySpecService, meteringService, props);
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService) {
                return new EqualDistribution(thesaurus, propertySpecService, meteringService);
            }
        },
        NEAREST_AVERAGE_VALUE_DAY(NEAREST_AVERAGE_VALUE_DAY_ESTIMATOR) {
            @Override
            Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService, Map<String, Object> props){
                return new NearestAvgValueDayEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService, props );
            }

            @Override
            AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService) {
                return new NearestAvgValueDayEstimator(thesaurus,propertySpecService,validationService,meteringService,timeService,calendarService);
            }
        };

        private final String implementation;

        String getImplementation() {
            return implementation;
        }

        EstimatorDefinition(String implementation) {
            this.implementation = implementation;
        }

        abstract Estimator create(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService, Map<String, Object> props);

        abstract AbstractEstimator createTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, CalendarService calendarService);


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
                .map(estimatorDefinition -> estimatorDefinition.create(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService, props))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported implementation " + implementation));
    }

    @Override
    public Estimator createTemplate(String implementation) {
        return estimatorDefinitions()
                .filter(estimatorDefinition -> estimatorDefinition.matches(implementation))
                .findFirst()
                .map(estimatorDefinition -> estimatorDefinition.createTemplate(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported implementation " + implementation));
    }
}
