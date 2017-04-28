/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validates the {@link SupportsTimeOfUseEventsFromEffectiveMetrologyConfigurations} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-23 (13:40)
 */
public class SupportsTimeOfUseEventsFromEffectiveMetrologyConfigurationsValidator implements ConstraintValidator<SupportsTimeOfUseEventsFromEffectiveMetrologyConfigurations, CalendarUsageImpl> {

    private final Thesaurus thesaurus;

    @Inject
    public SupportsTimeOfUseEventsFromEffectiveMetrologyConfigurationsValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(SupportsTimeOfUseEventsFromEffectiveMetrologyConfigurations supportsTimeOfUseEventsFromEffectiveMetrologyConfigurations) {
        // No need to hold on to the annotation for now
    }

    @Override
    public boolean isValid(CalendarUsageImpl calendarUsage, ConstraintValidatorContext context) {
        return !this.isTimeOfUse(calendarUsage) || this.isValidTimeOfUse(calendarUsage, context);
    }

    private boolean isTimeOfUse(CalendarUsageImpl calendarUsage) {
        return OutOfTheBoxCategory.TOU.name().equals(calendarUsage.getCalendar().getCategory().getName());
    }

    public boolean isValidTimeOfUse(CalendarUsageImpl calendarUsage, ConstraintValidatorContext context) {
        Set<Long> providedEventCodes =
            calendarUsage
                .getCalendar()
                .getEvents()
                .stream()
                .map(Event::getCode)
                .collect(Collectors.toSet());
        Set<Long> requestedEventCodes =
            calendarUsage.getUsagePoint()
                .getEffectiveMetrologyConfigurations(calendarUsage.getRange())
                .stream()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .flatMap(this::mandatoryReadingTypes)
                .map(ReadingType::getTou)
                .map(Long::new)
                .collect(Collectors.toSet());
        requestedEventCodes.remove(0L);  // Code 0 actually means not applicable
        Set<Long> missingEventCodes = new HashSet<>();
        for (Long requestedEventCode : requestedEventCodes) {
            if (!providedEventCodes.contains(requestedEventCode)) {
                missingEventCodes.add(requestedEventCode);
            }
        }
        if (!missingEventCodes.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(this.getErrorMessage(missingEventCodes))
                .addPropertyNode("calendar")
                .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }

    private Stream<ReadingType> mandatoryReadingTypes(UsagePointMetrologyConfiguration configuration) {
        return mandatoryContracts(configuration)
                .map(MetrologyContract::getDeliverables)
                .flatMap(Collection::stream)
                .map(ReadingTypeDeliverable::getReadingType);
    }

    private Stream<MetrologyContract> mandatoryContracts(UsagePointMetrologyConfiguration configuration) {
        return configuration
                .getContracts()
                .stream()
                .filter(MetrologyContract::isMandatory);
    }

    private String getErrorMessage(Set<Long> missingEventCodes) {
        return this.thesaurus
                .getFormat(PrivateMessageSeeds.UNSATISFIED_TOU)
                .format(missingEventCodes
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")));
    }

}