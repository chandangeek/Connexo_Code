/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.Status;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONArray;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CalendarResourceTest extends CalendarApplicationTest {
    private static final String DAY_TYPE_NAME = "Day type name";
    private static final String DAY_TYPE2_NAME = "Second Day type name";
    private static final String CALENDAR_NAME = "Test Calendar";
    private static final String CALENDAR_DESCRIPTION = "This is the calendar description";
    private static final String EVENT_NAME = "New event name";
    private static final String PERIOD_NAME = "Period name";
    private static final String PERIOD2_NAME = "Second Period name";
    private static final String PERIOD3_NAME = "Second Period name";

    @Test
    public void testGetCalendar() throws Exception {
        mockCalendar();
        Response response = target("/calendars/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("name")).isEqualTo(CALENDAR_NAME);
        assertThat(jsonModel.<String>get("description")).isEqualTo(CALENDAR_DESCRIPTION);
        assertThat(jsonModel.<String>get("category.name")).isEqualTo("ToU");
        assertThat(jsonModel.<Integer>get("events[0].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("events[0].name")).isEqualTo(EVENT_NAME);
        assertThat(jsonModel.<Integer>get("events[0].code")).isEqualTo(3);
        assertThat(jsonModel.<Integer>get("dayTypes[0].id")).isEqualTo(5);
        assertThat(jsonModel.<String>get("dayTypes[0].name")).isEqualTo(DAY_TYPE_NAME);
        assertThat(jsonModel.<Integer>get("dayTypes[0].ranges[0].event")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("dayTypes[0].ranges[0].fromHour")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("dayTypes[0].ranges[0].fromMinute")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("dayTypes[0].ranges[0].fromSecond")).isEqualTo(0);
        assertThat(jsonModel.<String>get("periods[0].name")).isEqualTo(PERIOD_NAME);
        assertThat(jsonModel.<String>get("periods[2].name")).isEqualTo(PERIOD3_NAME);
        assertThat(jsonModel.<Integer>get("periods[0].fromMonth")).isEqualTo(3);
        assertThat(jsonModel.<Integer>get("periods[0].fromDay")).isEqualTo(7);
    }

    private void mockCalendar() {
        Calendar calendar = mock(Calendar.class);
        when(calendar.getId()).thenReturn(1L);
        when(calendar.getName()).thenReturn(CALENDAR_NAME);
        when(calendar.getDescription()).thenReturn(CALENDAR_DESCRIPTION);
        when(calendar.getStartYear()).thenReturn(Year.of(2010));
        when(calendar.getEndYear()).thenReturn(Year.of(2020));
        when(calendar.getStatus()).thenReturn(Status.ACTIVE);

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("ToU");
        when(calendar.getCategory()).thenReturn(category);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(2L);
        when(event.getName()).thenReturn(EVENT_NAME);
        when(event.getCode()).thenReturn(3L);
        when(calendar.getEvents()).thenReturn(Collections.singletonList(event));

        DayType dayType = mock(DayType.class);
        EventOccurrence eventOccurrence = mock(EventOccurrence.class);
        when(eventOccurrence.getEvent()).thenReturn(event);
        when(eventOccurrence.getId()).thenReturn(4L);
        when(eventOccurrence.getFrom()).thenReturn(LocalTime.MIDNIGHT);
        when(dayType.getEventOccurrences()).thenReturn(Collections.singletonList(eventOccurrence));
        when(dayType.getId()).thenReturn(5L);
        when(dayType.getName()).thenReturn(DAY_TYPE_NAME);

        DayType dayType2 = mock(DayType.class);
        EventOccurrence eventOccurrence2 = mock(EventOccurrence.class);
        when(eventOccurrence2.getEvent()).thenReturn(event);
        when(eventOccurrence2.getId()).thenReturn(6L);
        when(eventOccurrence2.getFrom()).thenReturn(LocalTime.MIDNIGHT);
        when(dayType2.getEventOccurrences()).thenReturn(Collections.singletonList(eventOccurrence2));
        when(dayType2.getId()).thenReturn(7L);
        when(dayType2.getName()).thenReturn(DAY_TYPE2_NAME);

        ArrayList<DayType> dayTypes = new ArrayList<>();
        dayTypes.add(dayType);
        dayTypes.add(dayType2);

        when(calendar.getDayTypes()).thenReturn(dayTypes);

        PeriodTransition periodTransition = mock(PeriodTransition.class);
        Period period = mock(Period.class);
        when(period.getName()).thenReturn(PERIOD_NAME);
        when(period.getId()).thenReturn(1L);
        when(periodTransition.getPeriod()).thenReturn(period);
        for(DayOfWeek dayOfWeek: DayOfWeek.values()) {
            when(period.getDayType(dayOfWeek)).thenReturn(dayType);
        }
        when(periodTransition.getOccurrence()).thenReturn(LocalDate.of(2016,3,7));

        PeriodTransition periodTransition2 = mock(PeriodTransition.class);
        Period period2 = mock(Period.class);
        when(period2.getName()).thenReturn(PERIOD2_NAME);
        when(period2.getId()).thenReturn(2L);
        when(periodTransition2.getPeriod()).thenReturn(period2);
        for(DayOfWeek dayOfWeek: DayOfWeek.values()) {
            when(period2.getDayType(dayOfWeek)).thenReturn(dayType2);
        }
        when(periodTransition2.getOccurrence()).thenReturn(LocalDate.of(2016,4,7));

        PeriodTransition periodTransition3 = mock(PeriodTransition.class);
        Period period3 = mock(Period.class);
        when(period3.getName()).thenReturn(PERIOD3_NAME);
        when(periodTransition3.getPeriod()).thenReturn(period3);
        for(DayOfWeek dayOfWeek: DayOfWeek.values()) {
            when(period3.getDayType(dayOfWeek)).thenReturn(dayType2);
        }
        when(periodTransition3.getOccurrence()).thenReturn(LocalDate.of(2016,5,7));

        List<PeriodTransition> periodTransitions = new ArrayList<>();
        periodTransitions.add(periodTransition);
        periodTransitions.add(periodTransition2);
        periodTransitions.add(periodTransition3);

        when(calendar.getTransitions()).thenReturn(periodTransitions);
        when(calendarService.findCalendar(1)).thenReturn(Optional.of(calendar));
    }

    @Test
    public void testGetCalendarForWeek () throws Exception {
        mockCalendar();
        //DAY IS TUESDAY 05/04/2016
        Response response = target("/calendars/1").queryParam("weekOf",1459814400000L).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("id")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("weekTemplate[1].type")).isEqualTo(5);
        assertThat(jsonModel.<Integer>get("weekTemplate[1].type")).isEqualTo(5);
        assertThat(jsonModel.<Integer>get("weekTemplate[3].type")).isEqualTo(7);
        assertThat(jsonModel.<Integer>get("weekTemplate[4].type")).isEqualTo(7);
        assertThat(jsonModel.<Integer>get("weekTemplate[5].type")).isEqualTo(7);
        assertThat(jsonModel.<Integer>get("weekTemplate[6].type")).isEqualTo(7);
        assertThat(jsonModel.<Integer>get("weekTemplate[7].type")).isEqualTo(7);

        JSONArray periods = jsonModel.get("periods");
        assertThat(periods).hasSize(2);
    }

}
