/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.mdm.dataquality.DataQualityKpiResults;
import com.elster.jupiter.mdm.dataquality.DataQualityOverview;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.stream.Collectors;

public class DataQualityOverviewInfoFactory {

    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ValidationService validationService;
    private final EstimationService estimationService;

    @Inject
    public DataQualityOverviewInfoFactory(MeteringService meteringService, MetrologyConfigurationService metrologyConfigurationService,
                                          ValidationService validationService, EstimationService estimationService) {
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
        this.estimationService = estimationService;
    }

    public DataQualityOverviewInfo asInfo(DataQualityOverview overview) {
        DataQualityOverviewInfo info = new DataQualityOverviewInfo();
        info.usagePointName = overview.getUsagePointName();
        info.serviceCategory = meteringService.getServiceCategory(overview.getServiceKind()).map(HasName::getName).orElse(null);

        DataQualityOverview.UsagePointConfigurationOverview configuration = overview.getConfigurationOverview();
        info.metrologyConfiguration = new IdWithNameInfo(configuration.getMetrologyConfigurationId(), configuration.getMetrologyConfigurationName());
        metrologyConfigurationService.findMetrologyPurpose(configuration.getMetrologyPurposeId())
                .ifPresent(purpose -> info.metrologyContract = new IdWithNameInfo(configuration.getMetrologyContractId(), purpose.getName()));
        info.isEffectiveConfiguration = configuration.isEffective();

        DataQualityKpiResults results = overview.getDataQualityKpiResults();

        info.channelSuspects = results.getChannelSuspects();
        info.registerSuspects = results.getRegisterSuspects();
        info.lastSuspect = results.getLastSuspect();

        info.amountOfSuspects = results.getAmountOfSuspects();
        info.amountOfConfirmed = results.getAmountOfConfirmed();
        info.amountOfEstimates = results.getAmountOfEstimates();
        info.amountOfInformatives = results.getAmountOfInformatives();
        info.amountOfTotalEdited = results.getAmountOfAdded() + results.getAmountOfEdited() + results.getAmountOfRemoved();

        info.amountOfAdded = results.getAmountOfAdded();
        info.amountOfEdited = results.getAmountOfEdited();
        info.amountOfRemoved = results.getAmountOfRemoved();

        info.suspectsPerValidator = validationService.getAvailableValidators(QualityCodeSystem.MDM).stream()
                .map(validator -> new DataQualityOverviewInfo.NameValueInfo(validator.getDisplayName(), results.getAmountOfSuspectsBy(validator)))
                .sorted(Comparator.comparing(nameValueInfo -> nameValueInfo.name))
                .collect(Collectors.toList());
        info.estimatesPerEstimator = estimationService.getAvailableEstimators(QualityCodeSystem.MDM).stream()
                .map(estimator -> new DataQualityOverviewInfo.NameValueInfo(estimator.getDisplayName(), results.getAmountOfEstimatesBy(estimator)))
                .sorted(Comparator.comparing(nameValueInfo -> nameValueInfo.name))
                .collect(Collectors.toList());
        return info;
    }
}
