/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import org.joda.time.DateTime;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeDifferenceCommandImplTest extends CommonCommandImplTests {

    @Rule
    public TestRule timeZone = Using.timeZone("Europe/Brussels");

    @Mock
    private OfflineDevice offlineDevice;

    @Before
    public void doBefore() throws Exception {
        EventPublisherImpl eventPublisher = mock(EventPublisherImpl.class);
        when(executionContextServiceProvider.eventPublisher()).thenReturn(eventPublisher);
        when(executionContextServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(commandRootServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void largeTimeDifferenceTest() {
        Clock meterTime = Clock.fixed(new DateTime(2000, Calendar.FEBRUARY, 1, 0, 0, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock systemTime = Clock.fixed(new DateTime(2015, Calendar.OCTOBER, 18, 15, 0, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(systemTime);

        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getTime()).thenReturn(Date.from(meterTime.instant()));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        TimeDifferenceCommand timeDifferenceCommand = new TimeDifferenceCommandImpl(groupedDeviceCommand);
        timeDifferenceCommand.execute(deviceProtocol, newTestExecutionContext());
        assertEquals(new TimeDuration(495900000, TimeDuration.TimeUnit.SECONDS), timeDifferenceCommand.getTimeDifference().get());
    }

    @Test
    public void testToJournalMessageDescriptionTimeDifferenceNotRead() {
        BasicCheckCommand basicCheckCommand = mock(BasicCheckCommand.class);
        when(basicCheckCommand.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        TimeDifferenceCommandImpl command = new TimeDifferenceCommandImpl(groupedDeviceCommand);
        assertEquals(ComCommandDescriptionTitle.TimeDifferenceCommandImpl.getDescription() + " {timeDifference: not read}", command.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test
    public void testToJournalMessageDescription() {
        BasicCheckCommand basicCheckCommand = mock(BasicCheckCommand.class);
        when(basicCheckCommand.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        TimeDifferenceCommandImpl command = new TimeDifferenceCommandImpl(groupedDeviceCommand);
        DeviceProtocol protocol = mock(DeviceProtocol.class);
        when(protocol.getTime()).thenReturn(new Date());
        command.execute(protocol, newTestExecutionContext());

        assertFalse("Time difference is read, expecting the difference to be logged", command.toJournalMessageDescription(LogLevel.INFO).contains("not read"));
    }
}
