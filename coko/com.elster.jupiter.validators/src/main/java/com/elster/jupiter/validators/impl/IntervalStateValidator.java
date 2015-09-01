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
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class IntervalStateValidator extends AbstractValidator {

    static final String INTERVAL_FLAGS = "intervalFlags";

    private PossibleIntervalFlags possibleIntervalFlags = new PossibleIntervalFlags();
    private Set<Flag> selectedFlags;

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
        ListValue<IntervalFlag> property = (ListValue<IntervalFlag>) properties.get(INTERVAL_FLAGS);
        selectedFlags = property.getValues().stream().map(IntervalFlag::getFlag).collect(Collectors.toSet());
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
        PropertySpec property = getPropertySpecService().listValuePropertySpec(INTERVAL_FLAGS, true, possibleIntervalFlags, possibleIntervalFlags.flags);
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        return builder.add(property).build();
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
        List<Pair<? extends NlsKey, String>> pairs = new ArrayList<>();

        for (IntervalFlag flag : possibleIntervalFlags.getFlags()) {
            pairs.add(Pair.of(SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, flag.getTranslationKey()), flag.getName()));
        }

        return pairs;
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(INTERVAL_FLAGS);
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

    private class PossibleIntervalFlags implements CanFindByStringKey<IntervalFlag> {

        private final IntervalFlag[] flags = {
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

        @Override
        public Optional<IntervalFlag> find(String key) {
            for (IntervalFlag flagParameter : flags) {
                if (key.equals(flagParameter.getId())){
                    return Optional.of(flagParameter);
                }
            }
            return Optional.empty();
        }
        
        @Override
        public Class<IntervalFlag> valueDomain() {
            return IntervalFlag.class;
        }

        public IntervalFlag[] getFlags() {
            return flags;
        }
    }
}
