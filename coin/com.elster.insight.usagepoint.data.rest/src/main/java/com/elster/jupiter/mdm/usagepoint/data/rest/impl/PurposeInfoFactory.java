package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationTaskInfoFactory;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class PurposeInfoFactory {
    private final ValidationStatusFactory validationStatusFactory;
    private final ValidationService validationService;
    private final DataValidationTaskInfoFactory dataValidationTaskInfoFactory;

    @Inject
    public PurposeInfoFactory(ValidationStatusFactory validationStatusFactory, Thesaurus thesaurus, ValidationService validationService, TimeService timeService, DataValidationTaskInfoFactory dataValidationTaskInfoFactory) {
        this.validationStatusFactory = validationStatusFactory;
        this.dataValidationTaskInfoFactory = dataValidationTaskInfoFactory;
        this.validationService = validationService;
    }

    public PurposeInfo asInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract, boolean withValidationTasks) {
        PurposeInfo purposeInfo = new PurposeInfo();
        purposeInfo.id = metrologyContract.getId();
        purposeInfo.name = metrologyContract.getMetrologyPurpose().getName();
        purposeInfo.required = metrologyContract.isMandatory();
        purposeInfo.active = purposeInfo.required;
        purposeInfo.version = metrologyContract.getVersion();
        IdWithNameInfo status = new IdWithNameInfo();
        MetrologyContract.Status metrologyContractStatus = metrologyContract.getStatus(effectiveMetrologyConfiguration.getUsagePoint());
        status.id = metrologyContractStatus.isComplete() ? "complete" : "incomplete";
        status.name = metrologyContractStatus.getName();
        purposeInfo.status = status;
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).ifPresent(channelsContainer ->
                purposeInfo.validationInfo = validationStatusFactory.getValidationStatusInfo(effectiveMetrologyConfiguration, metrologyContract, channelsContainer.getChannels()));
        if (withValidationTasks) {
            purposeInfo.dataValidationTasks = validationService.findValidationTasksQuery()
                    .select(where("metrologyContract").isEqualTo(metrologyContract))
                    .stream()
                    .map(dataValidationTaskInfoFactory::asMinimalInfo)
                    .sorted(Comparator.comparing(info -> info.name))
                    .collect(Collectors.toList());
        }
        return purposeInfo;
    }
}
