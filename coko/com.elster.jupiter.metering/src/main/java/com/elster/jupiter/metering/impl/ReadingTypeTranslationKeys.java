/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
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

        public static String getFullAliasNameElement(com.elster.jupiter.cbo.Commodity commodity, Thesaurus thesaurus) {
            return Stream
                .of(values())
                .filter(each -> each.commodity.equals(commodity))
                .findFirst()
                    .map(key -> thesaurus.getFormat(key).format()).orElse("");
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

        public static String getFullAliasNameElement(TimeAttribute timeAttribute, Thesaurus thesaurus) {
            return "[" + thesaurus.getFormat(new MeasuringPeriod(timeAttribute).asTranslationKey()).format() + "]";
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

        public static String getFullAliasNameElement(MetricMultiplier multiplier, Thesaurus thesaurus) {
            if (multiplier != MetricMultiplier.ZERO) {
                return thesaurus.getFormat(new Multiplier(multiplier).asTranslationKey()).format();
            } else {
                return "";
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
                case ANGLEMIN: {
                    return "arcmin";
                }
                case ANGLESECOND: {
                    return "arcsec";
                }
                case NOTAPPLICABLE: {
                    return "None";
                }
                default: {
                    String symbol = this.unit.getSymbol();
                    if (is(symbol).emptyOrOnlyWhiteSpace()) {
                        return this.unit.getUnit().getName();
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

        public static String getFullAliasNameElement(ReadingTypeUnit unit, Thesaurus thesaurus) {
            if (unit != ReadingTypeUnit.NOTAPPLICABLE) {
                return thesaurus.getFormat(new Unit(unit).asTranslationKey()).format();
            } else {
                return "";
            }
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }

    }

    public static class UnitFields implements TranslationKey {
        private final ReadingTypeUnit unit;

        public UnitFields(ReadingTypeUnit unit) {
            super();
            this.unit = unit;
        }

        @Override
        public String getKey() {
            return "readingType.unit." + this.unit.name() + ".name";
        }

        @Override
        public String getDefaultFormat() {
                    String unitName = this.unit.getName();
                    if (is(unitName).emptyOrOnlyWhiteSpace()) {
                        return this.unit.name();
                    }
                    else {
                        return unitName;
                    }
        }

        public static Stream<TranslationKey> allKeys() {
            return Arrays.stream(ReadingTypeUnit.values())
                    .map(UnitFields::new)
                    .map(UnitFields::asTranslationKey);
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }

    }

    public static class UnitWithMultiplier {

        public static String getFullAliasNameElement(MetricMultiplier multiplier, ReadingTypeUnit unit, Thesaurus thesaurus) {
            return "(" +
                    Multiplier.getFullAliasNameElement(multiplier, thesaurus) +
                    Unit.getFullAliasNameElement(unit, thesaurus) +
                    ")";
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

        public static String getFullAliasNameElement(com.elster.jupiter.cbo.MacroPeriod macroPeriod, Thesaurus thesaurus) {
            return "[" + thesaurus.getFormat(new MacroPeriod(macroPeriod).asTranslationKey()).format() + "]";
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

        public static String getFullAliasNameElement(com.elster.jupiter.cbo.Phase phase, Thesaurus thesaurus) {
            return thesaurus.getFormat(new Phase(phase).asTranslationKey()).format();
        }

    }

    public static class TimeOfUse {

        public static TranslationKey translationKey() {
            return new SimpleTranslationKey("readingType.timeOfUse", "ToU");
        }

        public static String getFullAliasNameElement(int timeOfUse, Thesaurus thesaurus) {
            return thesaurus.getFormat(translationKey()).format() + " " + timeOfUse;
        }

    }

    public static class Aggregate implements TranslationKey {
        private final com.elster.jupiter.cbo.Aggregate aggregate;

        public Aggregate(com.elster.jupiter.cbo.Aggregate aggregate) {
            super();
            this.aggregate = aggregate;
        }

        @Override
        public String getKey() {
            return "readingType.aggregate." + this.aggregate.name();
        }

        @Override
        public String getDefaultFormat() {
            return this.aggregate.getDescription();
        }

        public static Stream<TranslationKey> allKeys() {
            return Stream
                    .of(com.elster.jupiter.cbo.Aggregate.values())
                    .map(Aggregate::new)
                    .map(Aggregate::asTranslationKey);
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }
    }

    public enum Accumulation implements TranslationKey {
        DELTA("readingType.accumulation.delta", "Delta", com.elster.jupiter.cbo.Accumulation.DELTADELTA),
        BULK("readingType.accumulation.bulk", "Bulk", com.elster.jupiter.cbo.Accumulation.BULKQUANTITY),
        SUM("readingType.accumulation.sum", "Sum", com.elster.jupiter.cbo.Accumulation.SUMMATION);

        private final String key;
        private final String defaultFormat;
        private final com.elster.jupiter.cbo.Accumulation accumulation;

        Accumulation(String key, String defaultFormat, com.elster.jupiter.cbo.Accumulation accumulation) {
            this.key = key;
            this.defaultFormat = defaultFormat;
            this.accumulation = accumulation;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }

        public static String getFullAliasNameElement(com.elster.jupiter.cbo.Accumulation accumulation, Thesaurus thesaurus) {
            return Stream
                    .of(values())
                    .filter(each -> each.accumulation.equals(accumulation))
                    .findFirst()
                    .map(key -> thesaurus.getFormat(key).format()).orElse("");
        }
    }

    public static class AccumulationFields implements TranslationKey {
        private final com.elster.jupiter.cbo.Accumulation accumulation;

        public AccumulationFields(com.elster.jupiter.cbo.Accumulation accumulation) {
            super();
            this.accumulation = accumulation;
        }

        @Override
        public String getKey() {
            return "readingType.accumulation." + this.accumulation.name();
        }

        @Override
        public String getDefaultFormat() {
            return this.accumulation.getDescription();
        }

        public static Stream<TranslationKey> allKeys() {
            return Stream
                    .of(com.elster.jupiter.cbo.Accumulation.values())
                    .map(AccumulationFields::new)
                    .map(AccumulationFields::asTranslationKey);
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }
    }


    public static class FlowDirection implements TranslationKey {
        private final com.elster.jupiter.cbo.FlowDirection flowDirection;

        public FlowDirection(com.elster.jupiter.cbo.FlowDirection flowDirection) {
            super();
            this.flowDirection = flowDirection;
        }

        @Override
        public String getKey() {
            return "readingType.flowDirection." + this.flowDirection.name();
        }

        @Override
        public String getDefaultFormat() {
            return this.flowDirection.getDescription();
        }

        public static Stream<TranslationKey> allKeys() {
            return Stream
                    .of(com.elster.jupiter.cbo.FlowDirection.values())
                    .map(FlowDirection::new)
                    .map(FlowDirection::asTranslationKey);
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }
    }

    public static class MeasurementKind implements TranslationKey {
        private final com.elster.jupiter.cbo.MeasurementKind measurementKind;

        public MeasurementKind(com.elster.jupiter.cbo.MeasurementKind measurementKind) {
            super();
            this.measurementKind = measurementKind;
        }

        @Override
        public String getKey() {
            return "readingType.measurementKind." + this.measurementKind.name();
        }

        @Override
        public String getDefaultFormat() {
            return this.measurementKind.getDescription();
        }

        public static Stream<TranslationKey> allKeys() {
            return Stream
                    .of(com.elster.jupiter.cbo.MeasurementKind.values())
                    .map(MeasurementKind::new)
                    .map(MeasurementKind::asTranslationKey);
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }
    }


    public static class CommodityFields implements TranslationKey {
        private final com.elster.jupiter.cbo.Commodity commodity;

        public CommodityFields(com.elster.jupiter.cbo.Commodity commodity) {
            super();
            this.commodity = commodity;
        }

        @Override
        public String getKey() {
            return "readingType.commodity." + this.commodity.name();
        }

        @Override
        public String getDefaultFormat() {
            return this.commodity.getDescription();
        }

        public static Stream<TranslationKey> allKeys() {
            return Stream
                    .of(com.elster.jupiter.cbo.Commodity.values())
                    .map(CommodityFields::new)
                    .map(CommodityFields::asTranslationKey);
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }
    }

    public enum Currency implements TranslationKey {
        NOTAPPLICABLE("XXX"),
        POUND("GBP"),
        EURO("EUR"),
        USDOLLAR("USD");

        private final int id;
        private final java.util.Currency currency;

        Currency(String currencyCode) {
            this.currency = java.util.Currency.getInstance(currencyCode);
            this.id = this.currency.getNumericCode() != 999 ? this.currency.getNumericCode() : 0;
        }

        @Override
        public String getKey() {
            return "readingType.currency." + this.currency.getCurrencyCode();
        }

        @Override
        public String getDefaultFormat() {
            return getCurrencyDefaultFormat(this.currency);
        }

        public int getCurrencyCode() {
            return id;
        }

        private TranslationKey asTranslationKey() {
            return new SimpleTranslationKey(this.getKey(), this.getDefaultFormat());
        }

        public static String getCurrencyDefaultFormat(java.util.Currency currency) {
            return currency != null && currency.getNumericCode() != 999 ? currency.getDisplayName(Locale.ENGLISH) : "Not applicable";
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
        UnitFields.allKeys().forEach(allKeys::add);
        Aggregate.allKeys().forEach(allKeys::add);
        Collections.addAll(allKeys, Accumulation.values());
        FlowDirection.allKeys().forEach(allKeys::add);
        MeasurementKind.allKeys().forEach(allKeys::add);
        CommodityFields.allKeys().forEach(allKeys::add);
        AccumulationFields.allKeys().forEach(allKeys::add);
        Arrays.stream(Currency.values()).map(Currency::asTranslationKey).forEach(allKeys::add);
        return allKeys;
    }

}
