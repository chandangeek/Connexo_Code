package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.*;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IntervalStateValidator extends AbstractValidator {

    //TODO replace by reading qualities

    static final String INTERVAL_FLAGS = "intervalFlags";
    private static final Set<String> SUPPORTED_APPLICATIONS = ImmutableSet.of("MDC", "INS");

    private Set<Flag> selectedFlags;
    private final IntervalFlag[] POSSIBLE_FLAGS = {
            new IntervalFlag(ProtocolReadingQualities.BADTIME, "badTime", "Bad time"),
            new IntervalFlag(ProtocolReadingQualities.BATTERY_LOW, "batteryLow", "Battery low"),
            new IntervalFlag(ProtocolReadingQualities.CONFIGURATIONCHANGE, "configurationChange", "Configuration change"),
            new IntervalFlag(ProtocolReadingQualities.CORRUPTED, "corrupted", "Corrupted"),
            new IntervalFlag(ProtocolReadingQualities.DEVICE_ERROR, "deviceError", "Device error"),
            new IntervalFlag(ProtocolReadingQualities.MISSING, "missing", "Missing"),
            new IntervalFlag(ProtocolReadingQualities.MODIFIED, "modified", "Modified"),
            new IntervalFlag(ProtocolReadingQualities.OTHER, "other", "Other"),
            new IntervalFlag(ProtocolReadingQualities.OVERFLOW, "overflow", "Overflow"),
            new IntervalFlag(ProtocolReadingQualities.PHASEFAILURE, "phaseFailure", "Phase failure"),
            new IntervalFlag(ProtocolReadingQualities.POWERDOWN, "powerDown", "Power down"),
            new IntervalFlag(ProtocolReadingQualities.POWERUP, "powerUp", "Power up"),
            new IntervalFlag(ProtocolReadingQualities.WATCHDOGRESET, "watchdogReset", "Watchdog reset"),
            new IntervalFlag(ProtocolReadingQualities.REVERSERUN, "reverseRun", "Reverse run"),
            new IntervalFlag(ProtocolReadingQualities.SHORTLONG, "shortLong", "Short long"),
            new IntervalFlag(ProtocolReadingQualities.TEST, "test", "Test")
    };

    private Set<ProtocolReadingQualities> selectedFlags;


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
        } else {
            selectedFlags = new HashSet<>();
            selectedFlags.add(((IntervalFlag) property).getFlag());
        }
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        List<? extends ReadingQualityRecord> readingQualities = intervalReadingRecord.getReadingQualities();

        //TODO replace by reading qualities
        //Set<ProfileStatus.Flag> readingFlags = intervalReadingRecord.getProfileStatus().getFlags();
        return Collections.disjoint(selectedFlags, readingQualities) ? ValidationResult.VALID : ValidationResult.SUSPECT;
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
                        .named(INTERVAL_FLAGS, TranslationKeys.INTERVAL_STATE_VALIDATOR)
                        .fromThesaurus(this.getThesaurus())
                        .addValues(POSSIBLE_FLAGS)
                        .markMultiValued()
                        .markExhaustive(PropertySelectionMode.LIST)
                        .finish());
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.INTERVAL_STATE_VALIDATOR.getDefaultFormat();
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

    @Override
    public Set<String> getSupportedApplications() {
        return SUPPORTED_APPLICATIONS;
    }

    private class IntervalFlagValueFactory implements ValueFactory <IntervalFlag> {

        @Override
        public IntervalFlag fromStringValue(String stringValue) {
            for (IntervalFlag flagParameter : POSSIBLE_FLAGS) {
                if (stringValue.equals(flagParameter.getId())) {
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
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, IntervalFlag value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    class IntervalFlag extends HasIdAndName {

        private ProtocolReadingQualities flag;
        private String id;
        private String name;

        IntervalFlag(ProtocolReadingQualities flag, String id, String name) {
            this.flag = flag;
            this.id = id;
            this.name = name;
        }

        public ProtocolReadingQualities getFlag() {
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