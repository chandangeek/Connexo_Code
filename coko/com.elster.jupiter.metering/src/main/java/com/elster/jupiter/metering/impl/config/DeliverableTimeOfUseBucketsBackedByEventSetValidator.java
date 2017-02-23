/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeliverableTimeOfUseBucketsBackedByEventSet} constraint
 * and will produce an issue for every deliverable that is not backed by an event set.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-20 (12:57)
 */
public class DeliverableTimeOfUseBucketsBackedByEventSetValidator implements ConstraintValidator<DeliverableTimeOfUseBucketsBackedByEventSet, MetrologyConfiguration> {

    private final Thesaurus thesaurus;

    @Inject
    public DeliverableTimeOfUseBucketsBackedByEventSetValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(DeliverableTimeOfUseBucketsBackedByEventSet annotation) {
        // Currently no need to keep track of the annotation
    }

    @Override
    public boolean isValid(MetrologyConfiguration metrologyConfiguration, ConstraintValidatorContext context) {
        Set<Long> eventCodes = metrologyConfiguration
                .getEventSets()
                .stream()
                .map(EventSet::getEvents)
                .flatMap(Collection::stream)
                .map(Event::getCode)
                .collect(Collectors.toSet());
        List<ReadingTypeDeliverable> deliverables =
            metrologyConfiguration
                .getContracts()
                .stream()
                .map(MetrologyContract::getDeliverables)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        /* Cannot use allMatch on the Stream as we want to call validate on all deliverables
         * to produce a constraint violation for all deliverables that need one
         * and not just for the first one. */
        boolean result = true;
        for (ReadingTypeDeliverable deliverable : deliverables) {
            if (!this.validate(context, deliverable, eventCodes)) {
                result = false;
            }
        }
        return result;
    }

    private boolean validate(ConstraintValidatorContext context, ReadingTypeDeliverable deliverable, Set<Long> eventCodes) {
        long eventCode = deliverable.getReadingType().getTou();
        if (!eventCodes.contains(eventCode)){
            String message = this.thesaurus.getFormat(PrivateMessageSeeds.DELIVERABLE_TOU_NOT_BACKED_BY_EVENTSET).format(Long.toString(eventCode), deliverable.getName());
            context
                .buildConstraintViolationWithTemplate(message)
                .addPropertyNode("deliverables")
                .addPropertyNode("tou")
                    .inIterable().atKey(deliverable.getReadingType().getMRID())
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
            return false;
        } else {
            return true;
        }
    }

}