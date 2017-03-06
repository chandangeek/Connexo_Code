/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.dataquality.DeviceDataQualityService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

public class DeviceDataQualityRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    DeviceDataQualityService deviceDataQualityService;
    @Mock
    DataQualityKpiService dataQualityKpiService;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    ValidationService validationService;
    @Mock
    EstimationService estimationService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;

    @Override
    protected Application getApplication() {
        DeviceDataQualityApplication application = new DeviceDataQualityApplication();
        application.setDeviceDataQualityService(deviceDataQualityService);
        application.setDataQualityKpiService(dataQualityKpiService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setValidationService(validationService);
        application.setEstimationService(estimationService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setNlsService(nlsService);
        return application;
    }
}