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
import java.util.stream.Stream;

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
        return timeZone.getID();  // CXO-7969
    }

    @Override
    public boolean validateValue(Object objectValue) throws InvalidValueException {
        if (objectValue instanceof String && super.validateValueIgnoreRequired(objectValue)) {
            String value = (String) objectValue;
            if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
                return false;
            }
            return checkForKnowTimeZoneId(value) || checkAccordingToDateTimeFormatter(value);
        }
        return false;
    }

    private boolean checkAccordingToDateTimeFormatter(String value) throws InvalidValueException {
        try {
            format.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidValueException(thesaurus.getFormat(MessageSeeds.TIME_ZONE_IS_NOT_VALID).format(),
                    MessageSeeds.TIME_ZONE_IS_NOT_VALID.getDefaultFormat(),
                    getName());
        }
        return true;
    }

    private boolean checkForKnowTimeZoneId(String value) {
        return Stream.of(TimeZone.getAvailableIDs()).filter(s -> s.equals(value)).findFirst().isPresent();
    }
}
