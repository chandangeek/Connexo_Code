package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedCalendarEvent;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CollectedCalendarDeviceCommand} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedCalendarDeviceCommandTest {

    private static final String ACTIVE_CALENDAR_NAME = "Active";
    private static final String PASSIVE_CALENDAR_NAME = "Passive";

    @Mock
    private CollectedCalendar collectedCalendar;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvicer;
    @Mock
    private ComServerDAO comServerDAO;

    @Before
    public void initializeMocks() {
        when(this.serviceProvicer.clock()).thenReturn(Clock.systemUTC());
    }

    @Test
    public void descriptionTitleIsNotEmpty() {
        CollectedCalendarDeviceCommand command = this.getTestInstance();

        // Business method
        String descriptionTitle = command.getDescriptionTitle();

        // Asserts
        assertThat(descriptionTitle).isNotEmpty();
    }

    @Test
    public void executeWithEmptyCollectedCalendar() {
        when(this.collectedCalendar.isEmpty()).thenReturn(true);
        when(this.collectedCalendar.getActiveCalendar()).thenReturn(Optional.empty());
        when(this.collectedCalendar.getPassiveCalendar()).thenReturn(Optional.empty());

        // Business method
        this.getTestInstance().doExecute(this.comServerDAO);

        // Asserts
        verify(this.comServerDAO, never()).updateCalendars(any(CollectedCalendar.class));
    }

    @Test
    public void toJournalEntryDescrptionWithEmptyCollectedCalendar() {
        when(this.collectedCalendar.isEmpty()).thenReturn(true);
        when(this.collectedCalendar.getActiveCalendar()).thenReturn(Optional.empty());
        when(this.collectedCalendar.getPassiveCalendar()).thenReturn(Optional.empty());
        CollectedCalendarDeviceCommand command = this.getTestInstance();
        DescriptionBuilder builder = mock(DescriptionBuilder.class);

        // Business method
        command.toJournalMessageDescription(builder, ComServer.LogLevel.DEBUG);

        // Asserts
        verifyNoMoreInteractions(builder);
    }

    @Test
    public void newEventWithEmptyCollectedCalendar() {
        when(this.collectedCalendar.isEmpty()).thenReturn(true);
        when(this.collectedCalendar.getActiveCalendar()).thenReturn(Optional.empty());
        when(this.collectedCalendar.getPassiveCalendar()).thenReturn(Optional.empty());
        CollectedCalendarDeviceCommand command = this.getTestInstance();

        // Business method
        Optional<CollectedCalendarEvent> optionalEvent = command.newEvent(new ArrayList<>());

        // Asserts
        assertThat(optionalEvent).isEmpty();
    }

    @Test
    public void executeWithActiveAndPassiveCollectedCalendars() {
        when(this.collectedCalendar.isEmpty()).thenReturn(false);
        when(this.collectedCalendar.getActiveCalendar()).thenReturn(Optional.of(ACTIVE_CALENDAR_NAME));
        when(this.collectedCalendar.getPassiveCalendar()).thenReturn(Optional.of(PASSIVE_CALENDAR_NAME));

        // Business method
        this.getTestInstance().doExecute(this.comServerDAO);

        // Asserts
        verify(this.comServerDAO).updateCalendars(any(CollectedCalendar.class));
    }

    @Test
    public void toJournalEntryDescrptionWithActiveAndPassiveCollectedCalendars() {
        when(this.collectedCalendar.isEmpty()).thenReturn(false);
        when(this.collectedCalendar.getActiveCalendar()).thenReturn(Optional.of(ACTIVE_CALENDAR_NAME));
        when(this.collectedCalendar.getPassiveCalendar()).thenReturn(Optional.of(PASSIVE_CALENDAR_NAME));
        CollectedCalendarDeviceCommand command = this.getTestInstance();
        DescriptionBuilder builder = mock(DescriptionBuilder.class);
        when(builder.addProperty(anyString())).thenReturn(new StringBuilder());

        // Business method
        command.toJournalMessageDescription(builder, ComServer.LogLevel.DEBUG);

        // Asserts
        verify(builder, times(2)).addProperty(anyString());
    }

    @Test
    public void newEventWithActiveAndPassiveCollectedCalendars() {
        when(this.collectedCalendar.isEmpty()).thenReturn(false);
        when(this.collectedCalendar.getActiveCalendar()).thenReturn(Optional.of(ACTIVE_CALENDAR_NAME));
        when(this.collectedCalendar.getPassiveCalendar()).thenReturn(Optional.of(PASSIVE_CALENDAR_NAME));
        CollectedCalendarDeviceCommand command = this.getTestInstance();
        List<Issue> expectedIssues = Arrays.asList(mock(Issue.class), mock(Issue.class));

        // Business method
        Optional<CollectedCalendarEvent> optionalEvent = command.newEvent(expectedIssues);

        // Asserts
        assertThat(optionalEvent).isPresent();
        assertThat(optionalEvent.get().getIssues()).isEqualTo(expectedIssues);
    }

    private CollectedCalendarDeviceCommand getTestInstance() {
        return new CollectedCalendarDeviceCommand(this.serviceProvicer, this.collectedCalendar, this.comTaskExecution);
    }

}