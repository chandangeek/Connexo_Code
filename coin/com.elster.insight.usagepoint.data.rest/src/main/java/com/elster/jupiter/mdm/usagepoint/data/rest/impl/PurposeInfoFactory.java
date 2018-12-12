/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationTaskInfoFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class PurposeInfoFactory {
    private final ValidationStatusFactory validationStatusFactory;
    private final ValidationService validationService;
    private final DataValidationTaskInfoFactory dataValidationTaskInfoFactory;
    private final Clock clock;
    private final Thesaurus thesaurus;

    @Inject
    public PurposeInfoFactory(ValidationStatusFactory validationStatusFactory, ValidationService validationService, DataValidationTaskInfoFactory dataValidationTaskInfoFactory,
                              Clock clock, Thesaurus thesaurus) {
        this.validationStatusFactory = validationStatusFactory;
        this.dataValidationTaskInfoFactory = dataValidationTaskInfoFactory;
        this.validationService = validationService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    public PurposeInfo asInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract, boolean withValidationTasks) {
        PurposeInfo purposeInfo = new PurposeInfo();
        purposeInfo.id = metrologyContract.getId();
        purposeInfo.name = metrologyContract.getMetrologyPurpose().getName();
        purposeInfo.description = metrologyContract.getMetrologyPurpose().getDescription();
        purposeInfo.required = metrologyContract.isMandatory();
        purposeInfo.active = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract, clock.instant()).isPresent();
        purposeInfo.version = metrologyContract.getVersion();
        IdWithNameInfo status = new IdWithNameInfo();
        status.id = effectiveMetrologyConfiguration.isComplete(metrologyContract) ? "complete" : "incomplete";
        status.name = effectiveMetrologyConfiguration.isComplete(metrologyContract) ?
                thesaurus.getFormat(DefaultMetrologyPurpose.Translation.METROLOGY_CONTRACT_STATUS_COMPLETE).format()
                : thesaurus.getFormat(DefaultMetrologyPurpose.Translation.METROLOGY_CONTRACT_STATUS_INCOMPLETE).format();
        purposeInfo.status = status;
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).ifPresent(channelsContainer ->
                purposeInfo.validationInfo = validationStatusFactory.getValidationStatusInfo(effectiveMetrologyConfiguration, metrologyContract, channelsContainer));
        if (withValidationTasks) {
            purposeInfo.dataValidationTasks = validationService.findValidationTasksQuery()
                    .select(where("metrologyContract").isEqualTo(metrologyContract))
                    .stream()
                    .map(dataValidationTaskInfoFactory::asMinimalInfo)
                    .sorted(Comparator.comparing(info -> info.name))
                    .collect(Collectors.toList());
        }
        purposeInfo.parent = new VersionInfo<>(effectiveMetrologyConfiguration.getUsagePoint()
                .getId(), effectiveMetrologyConfiguration.getUsagePoint().getVersion());
        purposeInfo.eventNames = new ArrayList<>();

        List<Long> longs = metrologyContract.getDeliverables()
                .stream()
                .map(readingTypeDeliverable -> (long) readingTypeDeliverable.getReadingType().getTou())
                .collect(Collectors.toList());

        List<Event> eventList = effectiveMetrologyConfiguration.getMetrologyConfiguration().getEventSets().stream()
                .flatMap(eventSet -> eventSet.getEvents().stream()).collect(Collectors.toList());


        eventList.stream().filter(event -> longs.contains(event.getCode())).forEach(event2 -> purposeInfo.eventNames.add(purposeInfo.eventNames.size(),event2.getName()));
        return purposeInfo;
    }
}
