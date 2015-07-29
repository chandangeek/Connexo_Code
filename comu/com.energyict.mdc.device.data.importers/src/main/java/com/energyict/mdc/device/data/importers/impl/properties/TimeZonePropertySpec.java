package com.energyict.mdc.device.data.importers.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpecPossibleValuesImpl;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeZonePropertySpec extends BasicPropertySpec {

    public static final String DEFAULT;
    static {
        TimeZone timeZone = TimeZone.getDefault();
        int rawOffset = timeZone.getRawOffset();
        long hours = TimeUnit.MILLISECONDS.toHours(rawOffset);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(rawOffset) - TimeUnit.HOURS.toMinutes(hours);
        DEFAULT = String.format("GMT%s%02d:%02d", rawOffset > 0 ? "+" : "-", hours, minutes);
    }

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
            if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
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
