package com.energyict.mdc.device.data.importers.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpecPossibleValuesImpl;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeZonePropertySpec extends BasicPropertySpec {

    static final String DEFAULT = "GMT+03:00";//IT MUST NOT BE DEFAULT VALUE
    public static DateTimeFormatter format = DateTimeFormatter.ofPattern("O");

    private Thesaurus thesaurus;

    public TimeZonePropertySpec(String name, Thesaurus thesaurus) {
        super(name, true, new StringFactory());
        setPossibleValues(new PropertySpecPossibleValuesImpl(DEFAULT, false));
        this.thesaurus = thesaurus;
    }

    @Override
    public boolean validateValue(Object objectValue) throws InvalidValueException {
        if (objectValue instanceof String && super.validateValueIgnoreRequired(objectValue)) {
            String value = (String) objectValue;
            if (value == null || value.isEmpty()) {
                return false;
            }
            try {
                format.parse(value);
            } catch (DateTimeParseException e) {
                throw new InvalidValueException(MessageSeeds.TIME_ZONE_IS_NOT_VALID.getTranslated(thesaurus),
                        MessageSeeds.TIME_ZONE_IS_NOT_VALID.getDefaultFormat(),
                        getName());
            }
            return true;
        }
        return false;
    }
}
