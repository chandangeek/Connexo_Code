/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Currency;

import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointConfigurationRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    Clock clock;
    @Mock
    JsonService jsonService;
    @Mock
    ValidationService validationService;
    @Mock
    EstimationService estimationService;
    @Mock
    UsagePointConfigurationService usagePointConfigurationService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    MeteringService meteringService;
    @Mock
    TimeService timeService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;
    @Mock
    FiniteStateMachineService finiteStateMachineService;
    @Mock
    UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @Override
    protected Application getApplication() {
        UsagePointConfigurationApplication application = new UsagePointConfigurationApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setJsonService(jsonService);
        application.setValidationService(validationService);
        application.setEstimationService(estimationService);
        application.setClockService(clock);
        application.setUsagePointConfigurationService(usagePointConfigurationService);
        application.setMetrologyConfigurationService(metrologyConfigurationService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setMeteringService(meteringService);
        application.setTimeService(timeService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        application.setFiniteStateMachineService(finiteStateMachineService);
        application.setUsagePointLifeCycleConfigurationService(usagePointLifeCycleConfigurationService);
        return application;
    }

    protected ReadingType mockReadingType() {
        return mockReadingType(0);
    }

    protected ReadingType mockReadingType(int timeOfUse) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("13.0.0.4.1.1.12.0.0.0.0." + timeOfUse + ".0.0.0.3.72.0");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getAggregate()).thenReturn(Aggregate.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getAccumulation()).thenReturn(Accumulation.NOTAPPLICABLE);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.NOTAPPLICABLE);
        when(readingType.getCommodity()).thenReturn(Commodity.NOTAPPLICABLE);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.NOTAPPLICABLE);
        when(readingType.getInterharmonic()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType.getArgument()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType.getTou()).thenReturn(timeOfUse);
        when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.NOTAPPLICABLE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("XXX"));
        return readingType;
    }
}