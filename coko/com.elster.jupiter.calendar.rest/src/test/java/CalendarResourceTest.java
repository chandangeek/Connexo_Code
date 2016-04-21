import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.rest.impl.CalendarApplication;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class CalendarResourceTest extends CalendarApplicationTest {
    private static final java.lang.String DAY_TYPE_NAME = "Day type name";
    private static final String CALENDAR_NAME = "Test Calendar";
    private static final String CALENDAR_DESCRIPTION = "This is the calendar description";
    private static final String EVENT_NAME = "New event name";
    private static final String PERIOD_NAME = "Period name";

    @Test
    public void testGetCalendar() throws Exception {
        mockCalendar();
        Response response = target("/calendars/timeofusecalendars/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("name")).isEqualTo(CALENDAR_NAME);
        assertThat(jsonModel.<String>get("description")).isEqualTo(CALENDAR_DESCRIPTION);
        assertThat(jsonModel.<String>get("category")).isEqualTo("ToU");
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
        assertThat(jsonModel.<Integer>get("periods[0].fromMonth")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("periods[0].fromDay")).isEqualTo(3);
    }

    private void mockCalendar() {
        Calendar calendar = mock(Calendar.class);
        when(calendar.getId()).thenReturn(1L);
        when(calendar.getName()).thenReturn(CALENDAR_NAME);
        when(calendar.getDescription()).thenReturn(CALENDAR_DESCRIPTION);

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("ToU");
        when(calendar.getCategory()).thenReturn(category);

        when(calendar.getTimeZone()).thenReturn(TimeZone.getDefault());

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
        when(calendar.getDayTypes()).thenReturn(Collections.singletonList(dayType));

        PeriodTransition periodTransition = mock(PeriodTransition.class);
        Period period = mock(Period.class);
        when(period.getName()).thenReturn(PERIOD_NAME);
        when(periodTransition.getPeriod()).thenReturn(period);
        for(DayOfWeek dayOfWeek: DayOfWeek.values()) {
            when(period.getDayType(dayOfWeek)).thenReturn(dayType);
        }
        when(periodTransition.getOccurrence()).thenReturn(LocalDate.of(2016,2,3));

        when(calendar.getTransitions()).thenReturn(Collections.singletonList(periodTransition));
        when(calendarService.findCalendar(1)).thenReturn(Optional.of(calendar));
    }

    @Test
    public void testGetCalendarForWeek () throws Exception {
        mockCalendar();
        //DAY IS THURSDAY 21/04/2016
        Response response = target("/calendars/timeofusecalendars/1").queryParam("weekOf",1461241773L).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("id")).isEqualTo(1);
    }

}
