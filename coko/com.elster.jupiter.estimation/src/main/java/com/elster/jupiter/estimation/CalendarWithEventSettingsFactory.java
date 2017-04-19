package com.elster.jupiter.estimation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.calendar.CalendarService;

import java.util.Map;

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
            if (calendarAndEvent.length < 2){
                discardDay = Boolean.parseBoolean(calendarAndEvent[0]);
                return new DiscardDaySettings(discardDay, calendar, event);
            } else if (calendarAndEvent.length < 3){
                discardDay = Boolean.parseBoolean(calendarAndEvent[0]);
                calendar = calendarService.findCalendar(Long.parseLong(calendarAndEvent[1])).orElse(null);
                return new DiscardDaySettings(discardDay, calendar, event);
            } else {
                discardDay = Boolean.parseBoolean(calendarAndEvent[0]);
                calendar = calendarService.findCalendar(Long.parseLong(calendarAndEvent[1])).orElse(null);
                event = calendar.getEvents()
                        .stream()
                        .filter(ev -> ev.getId() == Long.parseLong(calendarAndEvent[2])).findAny().orElse(null);
            }
            return new DiscardDaySettings(discardDay, calendar, event);
        }
    }

    @Override
    public String toStringValue(CalendarWithEventSettings object) {
        if (object instanceof NoneCalendarWithEventSettings){
            return NoneCalendarWithEventSettings.NONE_CALENDAR_SETTINGS;
        }
        DiscardDaySettings settings = (DiscardDaySettings) object;
        String fields = "";
            fields = settings.isDiscardDay().toString() + ":";
            fields += settings.getCalendar() != null ? settings.getCalendar().getId() + ":" : "";
            fields += settings.getEvent() != null ? settings.getEvent().getId() : "";
        return fields;
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
