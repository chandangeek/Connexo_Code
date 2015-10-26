package com.energyict.mdc.device.data.importers.impl.properties;

import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpecPossibleValuesImpl;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.util.Checks;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeZonePropertySpec extends BasicPropertySpec {

    public static DateTimeFormatter format = DateTimeFormatter.ofPattern("O");

    private Thesaurus thesaurus;

    public TimeZonePropertySpec(String name, Thesaurus thesaurus, Clock clock) {
        super(name, true, new StringFactory());
        setPossibleValues(new PropertySpecPossibleValuesImpl(getDefaultTimeZone(clock), false));
        this.thesaurus = thesaurus;
    }

    public String getDefaultTimeZone(Clock clock) {
        TimeZone timeZone = TimeZone.getTimeZone(clock.getZone());
        int rawOffset = timeZone.getOffset(clock.millis());
        long hours = TimeUnit.MILLISECONDS.toHours(rawOffset);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(rawOffset) - TimeUnit.HOURS.toMinutes(hours);
        return String.format("GMT%s%02d:%02d", rawOffset >= 0 ? "+" : "-", hours, minutes);
    }

    @Override
    public boolean validateValue(Object objectValue) throws InvalidValueException {
        if (objectValue instanceof String && super.validateValueIgnoreRequired(objectValue)) {
            String value = (String) objectValue;
            if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
                return false;
            }
            try {
                format.parse(value);
            } catch (DateTimeParseException e) {
                throw new InvalidValueException(thesaurus.getFormat(MessageSeeds.TIME_ZONE_IS_NOT_VALID).format(),
                        MessageSeeds.TIME_ZONE_IS_NOT_VALID.getDefaultFormat(),
                        getName());
            }
            return true;
        }
        return false;
    }
}
