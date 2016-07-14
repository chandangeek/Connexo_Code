package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class PurposeInfoFactory {
    private final ValidationStatusFactory validationStatusFactory;
    private final Thesaurus thesaurus;
    private final ValidationService validationService;
    private final TimeService timeService;

    @Inject
    public PurposeInfoFactory(ValidationStatusFactory validationStatusFactory, Thesaurus thesaurus, ValidationService validationService, TimeService timeService) {
        this.validationStatusFactory = validationStatusFactory;
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.timeService = timeService;
    }

    public PurposeInfo asInfo(MetrologyContract metrologyContract, UsagePoint usagePoint) {
        PurposeInfo purposeInfo = new PurposeInfo();
        purposeInfo.id = metrologyContract.getMetrologyPurpose().getId();
        purposeInfo.name = metrologyContract.getMetrologyPurpose().getName();
        purposeInfo.required = metrologyContract.isMandatory();
        purposeInfo.active = purposeInfo.required;
        IdWithNameInfo status = new IdWithNameInfo();
        MetrologyContract.Status metrologyContractStatus = metrologyContract.getStatus(usagePoint);
        status.id = metrologyContractStatus.isComplete() ? "complete" : "incomplete";
        status.name = metrologyContractStatus.getName();
        purposeInfo.status = status;
        return purposeInfo;
    }

    public PurposeInfo fullInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        PurposeInfo purposeInfo = asInfo(metrologyContract, effectiveMetrologyConfiguration.getUsagePoint());
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).ifPresent(channelsContainer ->
                purposeInfo.validationInfo = validationStatusFactory.getValidationStatusInfo(effectiveMetrologyConfiguration, metrologyContract, channelsContainer.getChannels()));
        purposeInfo.dataValidationTasks = validationService.findValidationTasksQuery()
                .select(where("metrologyContract").isEqualTo(metrologyContract))
                .stream()
                .map(validationTask -> new DataValidationTaskInfo(validationTask, thesaurus, timeService))
                .sorted(Comparator.comparing(info -> info.name))
                .collect(Collectors.toList());
        return purposeInfo;
    }
}
