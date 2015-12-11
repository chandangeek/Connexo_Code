package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ProfileStatus.Flag;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.Range;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IntervalStateValidator extends AbstractValidator {

    static final String INTERVAL_FLAGS = "intervalFlags";

    private Set<Flag> selectedFlags;
    private final IntervalFlag[] POSSIBLE_FLAGS = {
            new IntervalFlag(Flag.BADTIME, "badTime", "Bad time"),
            new IntervalFlag(Flag.BATTERY_LOW, "batteryLow", "Battery low"),
            new IntervalFlag(Flag.CONFIGURATIONCHANGE, "configurationChange", "Configuration change"),
            new IntervalFlag(Flag.CORRUPTED, "corrupted", "Corrupted"),
            new IntervalFlag(Flag.DEVICE_ERROR, "deviceError", "Device error"),
            new IntervalFlag(Flag.MISSING, "missing", "Missing"),
            new IntervalFlag(Flag.OTHER, "other", "Other"),
            new IntervalFlag(Flag.OVERFLOW, "overflow", "Overflow"),
            new IntervalFlag(Flag.PHASEFAILURE, "phaseFailure", "Phase failure"),
            new IntervalFlag(Flag.POWERDOWN, "powerDown", "Power down"),
            new IntervalFlag(Flag.POWERUP, "powerUp", "Power up"),
            new IntervalFlag(Flag.WATCHDOGRESET, "watchdogReset", "Watchdog reset"),
            new IntervalFlag(Flag.TEST, "test", "Test")
    };


    IntervalStateValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    IntervalStateValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }


    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        initParameters(properties);
    }

    @SuppressWarnings("unchecked")
    private void initParameters(Map<String, Object> properties) {
        Object property = properties.get(INTERVAL_FLAGS);
        if (property instanceof Collection) {
            selectedFlags = ((Collection<IntervalFlag>) property).stream().map(IntervalFlag::getFlag).collect(Collectors.toSet());
        }
        else {
            selectedFlags = new HashSet<>();
            selectedFlags.add(((IntervalFlag) property).getFlag());
        }
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        Set<ProfileStatus.Flag> readingFlags = intervalReadingRecord.getProfileStatus().getFlags();
        return Collections.disjoint(selectedFlags, readingFlags) ? ValidationResult.VALID : ValidationResult.SUSPECT;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return ValidationResult.VALID;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(
                getPropertySpecService()
                        .specForValuesOf(new IntervalFlagValueFactory())
                        .named(INTERVAL_FLAGS, INTERVAL_FLAGS)
                        .describedAs(INTERVAL_FLAGS)
                        .addValues(POSSIBLE_FLAGS)
                        .markMultiValued()
                        .markExhaustive(PropertySelectionMode.LIST)
                        .finish());
    }

    @Override
    public String getDefaultFormat() {
        return "Interval state";
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
            case INTERVAL_FLAGS:
                return "Interval flags";
            default:
                return null;
        }
    }

    @Override
    public List<Pair<? extends NlsKey, String>> getExtraTranslations() {
        return Stream.of(POSSIBLE_FLAGS)
                .map(flag -> Pair.of(SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, flag.getTranslationKey()), flag.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.singletonList(INTERVAL_FLAGS);
    }

    private class IntervalFlagValueFactory implements ValueFactory <IntervalFlag> {

        @Override
        public IntervalFlag fromStringValue(String stringValue) {
            for (IntervalFlag flagParameter : POSSIBLE_FLAGS) {
                if (stringValue.equals(flagParameter.getId())){
                    return flagParameter;
                }
            }
            return null;
        }

        @Override
        public String toStringValue(IntervalFlag object) {
            return object.getId();
        }

        @Override
        public Class<IntervalFlag> getValueType() {
            return IntervalFlag.class;
        }

        @Override
        public IntervalFlag valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(IntervalFlag object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, IntervalFlag value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            }
            else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, IntervalFlag value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            }
            else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    class IntervalFlag extends HasIdAndName {

        private Flag flag;
        private String id;
        private String name;

        IntervalFlag(Flag flag, String id, String name) {
            this.flag = flag;
            this.id = id;
            this.name = name;
        }

        public Flag getFlag() {
            return flag;
        }

        @Override
        public String getId() {
            return id;
        }

        public String getName() {
            return getThesaurus().getString(getTranslationKey(), name);
        }

        String getTranslationKey() {
            return getBaseKey() + "." + id;
        }
    }

}
