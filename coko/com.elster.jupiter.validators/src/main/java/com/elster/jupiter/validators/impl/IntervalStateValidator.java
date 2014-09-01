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
import com.elster.jupiter.properties.FindById;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.ListValueEntry;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MessageSeeds;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void init(Channel channel, ReadingType readingType, Interval interval) {
        initParameters(properties);
    }

    private void initParameters(Map<String, Object> properties) {
        selectedFlags = new HashSet<>();
        ListValue<IntervalFlag> property = (ListValue<IntervalFlag>) properties.get(INTERVAL_FLAGS);
        for (IntervalFlag parameter : property.getValues() ) {
            selectedFlags.add(parameter.getFlag());
        }
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        Set<ProfileStatus.Flag> readingFlags = intervalReadingRecord.getProfileStatus().getFlags();
        return Collections.disjoint(selectedFlags, readingFlags) ? ValidationResult.PASS : ValidationResult.SUSPECT;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return ValidationResult.PASS;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        PropertySpec<ListValue<IntervalFlag>> property = getPropertySpecService().listValuePropertySpec(INTERVAL_FLAGS, true, possibleIntervalFlags, possibleIntervalFlags.flags);
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
    
    private class IntervalFlag implements ListValueEntry {
        
        private Flag flag;
        private String id;
        private String name;

        public IntervalFlag(Flag flag, String id, String name) {
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
    
    private class PossibleIntervalFlags implements FindById<IntervalFlag> {
        
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
        public Optional<IntervalFlag> findById(String id) {
            for (IntervalFlag flagParameter : flags) {
                if (id.equalsIgnoreCase(flagParameter.getId())){
                    return Optional.of(flagParameter);
                }
            }
            return Optional.absent();
        }
        
        public IntervalFlag[] getFlags() {
            return flags;
        }
    }
}
