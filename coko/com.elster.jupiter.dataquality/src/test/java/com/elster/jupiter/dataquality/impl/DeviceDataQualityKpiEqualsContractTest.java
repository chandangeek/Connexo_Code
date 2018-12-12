/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;

import java.time.Clock;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class DeviceDataQualityKpiEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ValidationService validationService;
    @Mock
    private EstimationService estimationService;
    @Mock
    private MessageService messageService;
    @Mock
    private TaskService taskService;
    @Mock
    private KpiService kpiService;
    @Mock
    private Clock clock;

    private DeviceDataQualityKpiImpl deviceDataQualityKpi;

    @Override
    protected Object getInstanceA() {
        if (deviceDataQualityKpi == null) {
            deviceDataQualityKpi = new DeviceDataQualityKpiImpl(dataModel, meteringService, validationService, estimationService, messageService, taskService, kpiService, clock);
            setId(deviceDataQualityKpi, ID);
        }
        return deviceDataQualityKpi;
    }

    private void setId(DeviceDataQualityKpiImpl comSchedule, Long id) {
        field("id").ofType(Long.TYPE).in(comSchedule).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        DeviceDataQualityKpiImpl deviceDataQualityKpi = new DeviceDataQualityKpiImpl(dataModel, meteringService, validationService, estimationService, messageService, taskService, kpiService, clock);
        setId(deviceDataQualityKpi, ID);
        return deviceDataQualityKpi;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        DeviceDataQualityKpiImpl deviceDataQualityKpi = new DeviceDataQualityKpiImpl(dataModel, meteringService, validationService, estimationService, messageService, taskService, kpiService, clock);
        setId(deviceDataQualityKpi, OTHER_ID);
        return ImmutableSet.of(deviceDataQualityKpi);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}