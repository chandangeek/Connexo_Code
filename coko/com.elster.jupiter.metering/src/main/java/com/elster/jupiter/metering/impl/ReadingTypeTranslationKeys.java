package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides the translation keys for ReadingTypes
 */
public final class ReadingTypeTranslationKeys {

    public enum Commodity implements TranslationKey {
        PRIMARY("readingType.commodity.primary", "Primary", com.elster.jupiter.cbo.Commodity.ELECTRICITY_PRIMARY_METERED),
        SECONDARY("readingType.commodity.secondary", "Secondary", com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED);

        private final String key;
        private final String defaultFormat;
        private final com.elster.jupiter.cbo.Commodity commodity;

        Commodity(String key, String defaultFormat, com.elster.jupiter.cbo.Commodity commodity) {
            this.key = key;
            this.defaultFormat = defaultFormat;
            this.commodity = commodity;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }

        public static void appendFullAliasName(com.elster.jupiter.cbo.Commodity commodity, StringBuilder aliasNameBuilder, Thesaurus thesaurus) {
            Stream
                .of(values())
                .filter(each -> each.commodity.equals(commodity))
                .findFirst()
                .ifPresent(key -> aliasNameBuilder.append(" ").append(thesaurus.getFormat(key).format()).append(" "));
        }

    }

    public static class MeasuringPeriod implements TranslationKey {
        private final TimeAttribute timeAttribute;

        public MeasuringPeriod(TimeAttribute timeAttribute) {
            super();
            this.timeAttribute = timeAttribute;
        }

        @Override
        public String getKey() {
            return "readingType.measuringperiod." + this.timeAttribute.name();
        }

        @Override
        public String getDefaultFormat() {
            return this.timeAttribute.getDescription();
        }

        public static Stream<TranslationKey> allKeys() {
            return Stream
                    .of(TimeAttribute.values())
                    .map(MeasuringPeriod::new)
                    .map(MeasuringPeriod::asTranslationKey);
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }

        public static void appendFullAliasName(TimeAttribute timeAttribute, StringBuilder aliasNameBuilder, Thesaurus thesaurus) {
            aliasNameBuilder
                    .append("[")
                    .append(thesaurus
                                .getFormat(new MeasuringPeriod(timeAttribute).asTranslationKey())
                                .format())
                    .append("] ");
        }

    }

    public static class Multiplier implements TranslationKey {
        private final MetricMultiplier metricMultiplier;

        public Multiplier(MetricMultiplier metricMultiplier) {
            super();
            this.metricMultiplier = metricMultiplier;
        }

        @Override
        public String getKey() {
            return "readingType.multiplier." + this.metricMultiplier.name();
        }

        @Override
        public String getDefaultFormat() {
            return this.metricMultiplier.getSymbol();
        }

        public static Stream<TranslationKey> allKeys() {
            EnumSet<MetricMultiplier> allMultipliersExceptZero = EnumSet.complementOf(EnumSet.of(MetricMultiplier.ZERO));
            return allMultipliersExceptZero
                    .stream()
                    .map(Multiplier::new)
                    .map(Multiplier::asTranslationKey);
        }

        public static void appendFullAliasName(MetricMultiplier multiplier,  StringBuilder aliasNameBuilder, Thesaurus thesaurus) {
            if (multiplier != MetricMultiplier.ZERO) {
                aliasNameBuilder.append(thesaurus.getFormat(new Multiplier(multiplier).asTranslationKey()).format());
            }
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }

    }

    public static class Unit implements TranslationKey {
        private final ReadingTypeUnit unit;

        public Unit(ReadingTypeUnit unit) {
            super();
            this.unit = unit;
        }

        @Override
        public String getKey() {
            return "readingType.unit." + this.unit.name();
        }

        @Override
        public String getDefaultFormat() {
            switch (this.unit) {
                case ANGLEMIN: // Intentional fall-through
                case ANGLESECOND: {
                    return this.unit.name();
                }
                default: {
                    String symbol = this.unit.getSymbol();
                    if (is(symbol).emptyOrOnlyWhiteSpace()) {
                        return this.unit.name();
                    }
                    else {
                        return symbol;
                    }
                }
            }
        }

        public static Stream<TranslationKey> allKeys() {
            return allTranslatableReadingTypeUnits()
                    .stream()
                    .map(Unit::new)
                    .map(Unit::asTranslationKey);
        }

        private static EnumSet<ReadingTypeUnit> allTranslatableReadingTypeUnits() {
            return EnumSet.complementOf(EnumSet.of(ReadingTypeUnit.NOTAPPLICABLE));
        }

        public static void appendFullAliasName(ReadingTypeUnit unit,  StringBuilder aliasNameBuilder, Thesaurus thesaurus) {
            if (unit != ReadingTypeUnit.NOTAPPLICABLE) {
                aliasNameBuilder.append(thesaurus.getFormat(new Unit(unit).asTranslationKey()).format());
            }
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }

    }

    public static class UnitWithMultiplier {

        public static void appendFullAliasName(MetricMultiplier multiplier, ReadingTypeUnit unit, StringBuilder aliasNameBuilder, Thesaurus thesaurus) {
            aliasNameBuilder.append(" (");
            Multiplier.appendFullAliasName(multiplier, aliasNameBuilder, thesaurus);
            Unit.appendFullAliasName(unit, aliasNameBuilder, thesaurus);
            aliasNameBuilder.append(")");
        }

    }

    public static class MacroPeriod implements TranslationKey {
        private final com.elster.jupiter.cbo.MacroPeriod macroPeriod;

        public MacroPeriod(com.elster.jupiter.cbo.MacroPeriod macroPeriod) {
            super();
            this.macroPeriod = macroPeriod;
        }

        @Override
        public String getKey() {
            return "readingType.macroperiod." + this.macroPeriod.name();
        }

        @Override
        public String getDefaultFormat() {
            return this.macroPeriod.getDescription();
        }

        public static Stream<TranslationKey> allKeys() {
            return Stream
                    .of(com.elster.jupiter.cbo.MacroPeriod.values())
                    .map(MacroPeriod::new)
                    .map(MacroPeriod::asTranslationKey);
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }

        public static void appendFullAliasName(com.elster.jupiter.cbo.MacroPeriod macroPeriod, StringBuilder aliasNameBuilder, Thesaurus thesaurus) {
            aliasNameBuilder
                    .append("[")
                    .append(thesaurus
                                .getFormat(new MacroPeriod(macroPeriod).asTranslationKey())
                                .format())
                    .append("] ");
        }

    }

    public static class Phase implements TranslationKey {
        private final com.elster.jupiter.cbo.Phase phase;

        public Phase(com.elster.jupiter.cbo.Phase phase) {
            super();
            this.phase = phase;
        }

        @Override
        public String getKey() {
            return "readingType.phase." + this.phase.name();
        }

        @Override
        public String getDefaultFormat() {
            return this.phase.getDescription();
        }

        public static Stream<TranslationKey> allKeys() {
            return Stream
                    .of(com.elster.jupiter.cbo.Phase.values())
                    .map(Phase::new)
                    .map(Phase::asTranslationKey);
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }

        public static void appendFullAliasName(com.elster.jupiter.cbo.Phase phase, StringBuilder aliasNameBuilder, Thesaurus thesaurus) {
            aliasNameBuilder
                .append(" ")
                .append(thesaurus.getFormat(new Phase(phase).asTranslationKey()).format());
        }

    }

    public static class TimeOfUse {

        public static TranslationKey translationKey() {
            return new SimpleTranslationKey("readingType.timeOfUse", "Time of use");
        }

        public static void appendFullAliasName(int timeOfUse, StringBuilder aliasNameBuilder, Thesaurus thesaurus) {
            aliasNameBuilder
                .append(" ")
                .append(thesaurus.getFormat(translationKey()).format())
                .append(" ")
                .append(timeOfUse);
        }

    }

    public static List<TranslationKey> allKeys() {
        List<TranslationKey> allKeys = new ArrayList<>();
        allKeys.add(TimeOfUse.translationKey());
        Collections.addAll(allKeys, Commodity.values());
        MeasuringPeriod.allKeys().forEach(allKeys::add);
        MacroPeriod.allKeys().forEach(allKeys::add);
        Phase.allKeys().forEach(allKeys::add);
        Multiplier.allKeys().forEach(allKeys::add);
        Unit.allKeys().forEach(allKeys::add);
        return allKeys;
    }

}
