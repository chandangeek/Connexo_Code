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

import java.time.format.DateTimeFormatter;

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
            try {
                DateTimeFormatter.ofPattern(value);
            } catch (IllegalArgumentException e) {
                throw new InvalidValueException(thesaurus.getFormat(MessageSeeds.DATE_FORMAT_IS_NOT_VALID).format(),
                        MessageSeeds.DATE_FORMAT_IS_NOT_VALID.getDefaultFormat(),
                        getName());
            }
            return true;
        }
        return false;
    }
}
