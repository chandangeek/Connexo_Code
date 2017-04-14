package com.elster.jupiter.estimation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.calendar.CalendarService;

/**
 * Created by aeryomin on 05.04.2017.
 */
public class CalendarWithEventSettingsFactory extends AbstractValueFactory<CalendarWithEventSettings> {
    public static final String VALUE_UNIT_SEPARATOR = ":";

    private CalendarService calendarService;

    public CalendarWithEventSettingsFactory(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public Class<CalendarWithEventSettings> getValueType() {
        return CalendarWithEventSettings.class;
    }

    @Override
    protected int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public CalendarWithEventSettings fromStringValue(String stringValue) {
        Calendar calendar = null;
        Event event = null;
        Boolean discardDay = false;
        if (NoneCalendarWithEventSettings.NONE_CALENDAR_SETTINGS.equals(stringValue)) {
            return NoneCalendarWithEventSettings.INSTANCE;
        } else {
            String[] calendarAndEvent = stringValue.split(VALUE_UNIT_SEPARATOR);
            discardDay = Boolean.parseBoolean(calendarAndEvent[0]);
            if (discardDay == true) {
                Long eventId = Long.parseLong(calendarAndEvent[2]);
                calendar = calendarService.findCalendar(Long.parseLong(calendarAndEvent[1])).orElse(null);
                event = calendar.getEvents()
                        .stream()
                        .filter(ev -> ev.getId() == eventId).findAny().orElse(null);
            }
            return new DiscardDaySettings(discardDay, calendar, event);
        }
    }

    @Override
    public String toStringValue(CalendarWithEventSettings object) {
        return object.toString();
    }

    @Override
    public CalendarWithEventSettings valueFromDatabase(Object object) {
        return fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(CalendarWithEventSettings object) {
        return toStringValue(object);
    }
}
