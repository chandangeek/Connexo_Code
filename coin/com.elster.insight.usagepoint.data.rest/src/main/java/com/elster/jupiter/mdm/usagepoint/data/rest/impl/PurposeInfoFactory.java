package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.inject.Inject;
import java.util.Collections;

public class PurposeInfoFactory {
    private final ValidationStatusFactory validationStatusFactory;

    @Inject
    public PurposeInfoFactory(ValidationStatusFactory validationStatusFactory) {
        this.validationStatusFactory = validationStatusFactory;
    }

    public PurposeInfo asInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
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
        purposeInfo.dataValidationTasks = Collections.emptyList();
        return purposeInfo;
    }
}
