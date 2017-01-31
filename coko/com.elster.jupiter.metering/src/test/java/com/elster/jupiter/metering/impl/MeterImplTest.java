/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterAlreadyActive;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.Range;

import javax.inject.Provider;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterImplTest {

    public static final ZonedDateTime START = ZonedDateTime.of(2017, 5, 14, 4, 1, 14, 0, ZoneId.systemDefault());
    private static final String HEADEND_INTERFACE_NAME = UUID.randomUUID().toString();
    @Rule
    public TestRule zoneRule = Using.timeZoneOfMcMurdo();

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Provider<EndDeviceEventRecordImpl> deviceEventFactory;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private ServerMetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Provider<MeterActivationImpl> meterActivationFactory;
    @Mock
    private Clock clock;
    @Mock
    private Provider<ChannelBuilder> channelBuilderFactory;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private MeterRole meterRole;
    @Mock
    private HeadEndInterface headEndInterface;

    @Before
    public void setUp() {
        when(meteringService.getClock()).thenReturn(clock);
        when(meteringService.getDataModel()).thenReturn(dataModel);

        doAnswer(invocation -> new MeterActivationImpl(dataModel, eventService, clock, thesaurus)).when(meterActivationFactory).get();
        when(dataModel.getInstance(MeterActivationChannelsContainerImpl.class)).then(invocation -> new MeterActivationChannelsContainerImpl(meteringService, eventService, channelBuilderFactory));
        when(thesaurus.forLocale(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT)).thenReturn(meterRole);
        when(dataModel.getInstance(MeteringService.class)).thenReturn(meteringService);
        when(amrSystem.getName()).thenReturn(HEADEND_INTERFACE_NAME);
        when(meteringService.getHeadEndInterface(eq(HEADEND_INTERFACE_NAME))).thenReturn(Optional.empty());
    }

    @Test
    public void testNoOverlapOnActivate() {
        MeterImpl meter = new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService);
        meter.init(amrSystem, "1", "Name", null);

        MeterActivation meterActivation = meter.activate(START.toInstant());

        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(START.toInstant()));
        assertThat(meterActivation.getMeter()).contains(meter);
        assertThat(meterActivation.getUsagePoint()).isEmpty();
        verify(meteringService).getHeadEndInterface(HEADEND_INTERFACE_NAME);
    }

    @Test
    public void testActivateWithUsagePoint() {
        MeterImpl meter = new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService);
        meter.init(amrSystem, "1", "Name", null);

        MeterActivation meterActivation = meter.activate(usagePoint, START.toInstant());

        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(START.toInstant()));
        assertThat(meterActivation.getMeter()).contains(meter);
        assertThat(meterActivation.getUsagePoint()).contains(usagePoint);
        verify(meteringService).getHeadEndInterface(HEADEND_INTERFACE_NAME);
    }

    @Test(expected = MeterAlreadyActive.class)
    public void testOverlapOnActivate() {
        MeterImpl meter = new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService);
        meter.init(amrSystem, "1", "Name", null);

        meter.activate(START.toInstant());
        meter.activate(START.minusMonths(1).toInstant());
    }

    @Test
    public void testSetName() {
        MeterImpl meter = new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService)
                .init(amrSystem, "amrID", "Name", null);
        assertThat(meter.getName()).isEqualTo("Name");
        meter.setName("name42");
        assertThat(meter.getName()).isEqualTo("name42");
    }

    @Test
    public void getMeterActivationsRangeWithSingleMATest() {
        MeterImpl meter = new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService);
        meter.init(amrSystem, "1", "Name", null);

        MeterActivation meterActivation = meter.activate(START.toInstant());

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations(Range.atLeast(START.toInstant()
                .plus(1, ChronoUnit.DAYS)));
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations).containsExactly(meterActivation);
    }

    @Test
    public void getMeterActivationsRangeWithSingleMAOnBoundaryTest() {
        MeterImpl meter = new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService);
        meter.init(amrSystem, "1", "Name", null);

        MeterActivation meterActivation = meter.activate(START.toInstant());

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations(Range.atLeast(START.toInstant()));
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations).containsExactly(meterActivation);
    }

    @Test
    public void getMeterActivationsRangeWithSingleMAOverLappingTest() {
        MeterImpl meter = new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService);
        meter.init(amrSystem, "1", "Name", null);

        MeterActivation meterActivation = meter.activate(START.toInstant());

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations(Range.atLeast(START.toInstant()
                .minus(1, ChronoUnit.DAYS)));
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations).containsExactly(meterActivation);
    }

    @Test
    public void getMeterActivationsRangeWithTwoMATest() {
        MeterImpl meter = new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService);
        meter.init(amrSystem, "1", "Name", null);

        MeterActivation meterActivation1 = meter.activate(START.toInstant().minus(1, ChronoUnit.DAYS));
        meterActivation1.endAt(START.toInstant());
        MeterActivation meterActivation2 = meter.activate(START.toInstant());

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations(Range.atLeast(START.toInstant()
                .minus(10, ChronoUnit.DAYS)));
        assertThat(meterActivations).hasSize(2);
        assertThat(meterActivations).containsExactly(meterActivation1, meterActivation2);
    }

    @Test
    public void getMeterActivationsRangeWithTwoMAOnBoundaryTest() {
        MeterImpl meter = new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService);
        meter.init(amrSystem, "1", "Name", null);

        MeterActivation meterActivation1 = meter.activate(START.toInstant().minus(1, ChronoUnit.DAYS));
        meterActivation1.endAt(START.toInstant());
        MeterActivation meterActivation2 = meter.activate(START.toInstant());

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations(Range.atLeast(START.toInstant()));
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations).containsExactly(meterActivation2);
    }
}
