package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.MeterAlreadyActive;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterImplTest {

    public static final ZonedDateTime START = ZonedDateTime.of(2017, 5, 14, 4, 1, 14, 0, ZoneId.systemDefault());
    @Rule
    public TestRule zoneRule = Using.timeZoneOfMcMurdo();

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Provider<EndDeviceEventRecordImpl> deviceEventFactory;
    @Mock
    private MeteringService meteringService;
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

    @Before
    public void setUp() {
        doAnswer(invocation -> new MeterActivationImpl(dataModel, eventService, clock, channelBuilderFactory, thesaurus)).when(meterActivationFactory).get();

       when(thesaurus.forLocale(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
    }

    @Test
    public void testNoOverlapOnActivate() {
        MeterImpl meter = new MeterImpl(dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory);

        MeterActivationImpl meterActivation = meter.activate(START.toInstant());

        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(START.toInstant()));
        assertThat(meterActivation.getMeter()).contains(meter);
        assertThat(meterActivation.getUsagePoint()).isAbsent();

    }

    @Test
    public void testActivateWithUsagePoint() {
        MeterImpl meter = new MeterImpl(dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory);

        MeterActivationImpl meterActivation = meter.activate(usagePoint, START.toInstant());

        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(START.toInstant()));
        assertThat(meterActivation.getMeter()).contains(meter);
        assertThat(meterActivation.getUsagePoint()).contains(usagePoint);

    }

    @Test(expected = MeterAlreadyActive.class)
    public void testOverlapOnActivate() {
        MeterImpl meter = new MeterImpl(dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory);

        MeterActivationImpl meterActivation = meter.activate(START.toInstant());
        meter.activate(START.minusMonths(1).toInstant());

    }

    @Test
    public void testSetName() {
        MeterImpl meter = new MeterImpl(dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory).init(amrSystem, "amrID", "mrId");
        meter.setName("name42");

        assertThat(meter.getName()).isEqualTo("name42");

    }


}