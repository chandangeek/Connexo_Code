/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.slp.importers.impl.MessageSeeds;
import com.elster.jupiter.util.Checks;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeZonePropertySpec extends BasicPropertySpec {

    public static DateTimeFormatter format = DateTimeFormatter.ofPattern("O");

    private Thesaurus thesaurus;

    public TimeZonePropertySpec(Thesaurus thesaurus, Clock clock) {
        super(new StringFactory());
        setPossibleValues(new PropertySpecPossibleValuesImpl(getDefaultTimeZone(clock), false));
        this.thesaurus = thesaurus;
    }

    public TimeZonePropertySpec(String name, TranslationKey displayName, Thesaurus thesaurus, Clock clock) {
        this(thesaurus, clock);
        this.setName(name);
        this.setDisplayName(thesaurus.getFormat(displayName).format());
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
