/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.core.Application;

import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class EstimationApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    EstimationService estimationService;
    @Mock
    MeteringService meteringService;
    @Mock
    TimeService timeService;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    TaskService taskService;

    @Override
    protected Application getApplication() {
        EstimationApplication estimationApplication = new EstimationApplication();
        estimationApplication.setEstimationService(estimationService);
        estimationApplication.setRestQueryService(restQueryService);
        estimationApplication.setNlsService(nlsService);
        estimationApplication.setTimeService(timeService);
        estimationApplication.setMeteringGroupsService(meteringGroupsService);
        estimationApplication.setPropertyValueInfoService(propertyValueInfoService);
        estimationApplication.setMetrologyConfigurationService(metrologyConfigurationService);
        estimationApplication.setMeteringService(meteringService);
        estimationApplication.setTaskService(taskService);
        return estimationApplication;
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(transactionService.execute(Matchers.any())).thenAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform());
    }
}