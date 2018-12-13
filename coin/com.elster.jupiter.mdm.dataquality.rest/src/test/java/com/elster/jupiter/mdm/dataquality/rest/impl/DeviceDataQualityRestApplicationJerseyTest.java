/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.mdm.dataquality.UsagePointDataQualityService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.validation.ValidationService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

public class DeviceDataQualityRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    UsagePointDataQualityService usagePointDataQualityService;
    @Mock
    DataQualityKpiService dataQualityKpiService;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    MeteringService meteringService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    ValidationService validationService;
    @Mock
    EstimationService estimationService;

    @Override
    protected Application getApplication() {
        UsagePointDataQualityApplication application = new UsagePointDataQualityApplication();
        application.setUsagePointDataQualityService(usagePointDataQualityService);
        application.setDataQualityKpiService(dataQualityKpiService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setMeteringService(meteringService);
        application.setMetrologyConfigurationService(metrologyConfigurationService);
        application.setValidationService(validationService);
        application.setEstimationService(estimationService);
        application.setNlsService(nlsService);
        return application;
    }
}