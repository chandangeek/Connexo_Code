/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import java.util.stream.Stream;

import static com.energyict.mdc.device.data.importers.impl.properties.TimeZonePropertySpec.format;

public class DateFormatPropertySpec extends BasicPropertySpec {

    static final String DEFAULT = "dd/MM/yyyy HH:mm";

    private Thesaurus thesaurus;

    public DateFormatPropertySpec(Thesaurus thesaurus) {
        super(new StringFactory());
        setPossibleValues(new PropertySpecPossibleValuesImpl(DEFAULT, false));
        this.thesaurus = thesaurus;
    }

    public DateFormatPropertySpec(String name, TranslationKey displayName, Thesaurus thesaurus) {
        this(thesaurus);
        this.setName(name);
        this.setDisplayName(thesaurus.getFormat(displayName).format());
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
