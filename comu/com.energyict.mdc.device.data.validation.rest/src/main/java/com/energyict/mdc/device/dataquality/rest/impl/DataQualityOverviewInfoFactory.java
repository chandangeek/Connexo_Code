/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.dataquality.DataQualityOverview;
import com.energyict.mdc.device.dataquality.DeviceDataQualityKpiResults;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.stream.Collectors;

public class DataQualityOverviewInfoFactory {

    private final ValidationService validationService;
    private final EstimationService estimationService;

    @Inject
    public DataQualityOverviewInfoFactory(ValidationService validationService, EstimationService estimationService) {
        this.validationService = validationService;
        this.estimationService = estimationService;
    }

    public DataQualityOverviewInfo asInfo(DataQualityOverview overview) {
        DataQualityOverviewInfo info = new DataQualityOverviewInfo();
        info.deviceName = overview.getDeviceName();
        info.deviceSerialNumber = overview.getDeviceSerialNumber();
        info.deviceType = new IdWithNameInfo(overview.getDeviceType());
        info.deviceConfig = new IdWithNameInfo(overview.getDeviceConfiguration());

        DeviceDataQualityKpiResults results = overview.getDataQualityKpiResults();

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

        info.suspectsPerValidator = validationService.getAvailableValidators(QualityCodeSystem.MDC).stream()
                .map(validator -> new DataQualityOverviewInfo.NameValueInfo(validator.getDisplayName(), results.getAmountOfSuspectsBy(validator)))
                .sorted(Comparator.comparing(nameValueInfo -> nameValueInfo.name))
                .collect(Collectors.toList());
        info.estimatesPerEstimator = estimationService.getAvailableEstimators(QualityCodeSystem.MDC).stream()
                .map(estimator -> new DataQualityOverviewInfo.NameValueInfo(estimator.getDisplayName(), results.getAmountOfEstimatesBy(estimator)))
                .sorted(Comparator.comparing(nameValueInfo -> nameValueInfo.name))
                .collect(Collectors.toList());
        return info;
    }
}
