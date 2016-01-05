package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingTypeFieldsFactory;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadingTypeLocalizedFieldsFactory implements ReadingTypeFieldsFactory {

    final Thesaurus thesaurus;

    public ReadingTypeLocalizedFieldsFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public Map<Integer, String> getCodeFields(String field) {
        return Arrays.stream(ReadingTypeCodes.values())
                .filter(e -> e.getName().equals(field))
                .findFirst().orElseThrow(IllegalArgumentException::new)
                .getCodeInfo(thesaurus);
    }

    public enum ReadingTypeCodes {
        MACRO_PERIOD(ReadingTypeFilter.ReadingTypeFields.MACRO_PERIOD.getName(),
                (thesaurus) -> Arrays.stream(MacroPeriod.values()).filter(e -> e.getId() != 0)
                        .collect(Collectors.<MacroPeriod, Integer, String>toMap(MacroPeriod::getId, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(c)).format()))),
        AGGREGATE(ReadingTypeFilter.ReadingTypeFields.AGGREAGTE.getName(),
                (thesaurus) -> Arrays.stream(Aggregate.values()).filter(e -> e.getId() != 0)
                        .collect(Collectors.<Aggregate, Integer, String>toMap(Aggregate::getId, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.Aggregate(c)).format()))),
        MEASUREMENT_PERIOD(ReadingTypeFilter.ReadingTypeFields.MEASUREMENT_PERIOD.getName(),
                (thesaurus) -> Arrays.stream(TimeAttribute.values()).filter(e -> e.getId() != 0)
                        .collect(Collectors.<TimeAttribute, Integer, String>toMap(TimeAttribute::getId, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.MeasuringPeriod(c)).format()))),
        ACCUMULATION(ReadingTypeFilter.ReadingTypeFields.ACCUMULATION.getName(),
                (thesaurus) -> Arrays.stream(Accumulation.values()).filter(e -> e.getId() != 0)
                        .collect(Collectors.<Accumulation, Integer, String>toMap(Accumulation::getId, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.AccumulationFields(c)).format()))),
        FLOW_DIRECTION(ReadingTypeFilter.ReadingTypeFields.FLOW_DIRECTION.getName(),
                (thesaurus) -> Arrays.stream(FlowDirection.values()).filter(e -> e.getId() != 0)
                        .collect(Collectors.<FlowDirection, Integer, String>toMap(FlowDirection::getId, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.FlowDirection(c)).format()))),
        COMMODITY(ReadingTypeFilter.ReadingTypeFields.COMMODITY.getName(),
                (thesaurus) -> Arrays.stream(Commodity.values()).filter(e -> e.getId() != 0)
                        .collect(Collectors.<Commodity, Integer, String>toMap(Commodity::getId, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.CommodityFields(c)).format()))),
        MEASUREMENT_KIND(ReadingTypeFilter.ReadingTypeFields.MEASUREMENT_KIND.getName(),
                (thesaurus) -> Arrays.stream(MeasurementKind.values()).filter(e -> e.getId() != 0)
                        .collect(Collectors.<MeasurementKind, Integer, String>toMap(MeasurementKind::getId, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.MeasurementKind(c)).format()))),
        INTERHARMONIC_NUMERATOR(ReadingTypeFilter.ReadingTypeFields.INTERHARMONIC_NUMERATOR.getName(),
                (thesaurus) -> Stream.of(1, 2, 3, 4, 5, 6, 7)
                        .collect(Collectors.<Integer, Integer, String>toMap(Function.identity(), String::valueOf))),
        INTERHARMONIC_DENOMINATOR(ReadingTypeFilter.ReadingTypeFields.INTERHARMONIC_DENOMINATOR.getName(),
                (thesaurus) -> Stream.of(1, 2)
                        .collect(Collectors.<Integer, Integer, String>toMap(Function.identity(), String::valueOf))),
        ARGUMENT_NUMERATOR(ReadingTypeFilter.ReadingTypeFields.ARGUMENT_NUMERATOR.getName(),
                (thesaurus) -> Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 30, 45, 60, 12, 155, 240, 305, 360, 480, 720)
                        .collect(Collectors.<Integer, Integer, String>toMap(Function.identity(), String::valueOf))),
        ARGUMENT_DENOMINATOR(ReadingTypeFilter.ReadingTypeFields.ARGUMENT_DENOMINATOR.getName(),
                (thesaurus) -> Stream.of(1, 60, 120, 180, 240, 360)
                        .collect(Collectors.<Integer, Integer, String>toMap(Function.identity(), String::valueOf))),
        TIME_OF_USE("timeOfUse",
                (thesaurus) -> Stream.of(1, 2, 3, 4, 5).collect(Collectors.<Integer, Integer, String>toMap(Function.identity(), String::valueOf))),
        CPP("criticalPeakPeriod",
                (thesaurus) -> Stream.of(1, 2, 3, 4, 5, 6, 7)
                        .collect(Collectors.<Integer, Integer, String>toMap(Function.identity(), String::valueOf))),
        CONSUMPTION_TIER(ReadingTypeFilter.ReadingTypeFields.CONSUMPTION_TIER.getName(),
                (thesaurus) -> Stream.of(1, 2, 3, 4, 5, 6, 7)
                        .collect(Collectors.<Integer, Integer, String>toMap(Function.identity(), String::valueOf))),
        PHASES(ReadingTypeFilter.ReadingTypeFields.PHASES.getName(),
                (thesaurus) -> Arrays.stream(Phase.values()).filter(e -> e.getId() != 0).distinct()
                        .collect(Collectors.<Phase, Integer, String>toMap(Phase::getId, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.Phase(c)).format(), (s1, s2) -> s1))),
        MULTIPLIER(ReadingTypeFilter.ReadingTypeFields.MULTIPLIER.getName(),
                (thesaurus) -> Arrays.stream(MetricMultiplier.values()).filter(e -> e.getId() != 0)
                        .collect(Collectors.<MetricMultiplier, Integer, String>toMap(c -> Long.valueOf(c.getId()).byteValue() & 0xFF, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.Multiplier(c)).format()))),
        CURRENCY(ReadingTypeFilter.ReadingTypeFields.CURRENCY.getName(),
                (thesaurus) -> Arrays.stream(ReadingTypeTranslationKeys.Currency.values())
                        .collect(Collectors.<ReadingTypeTranslationKeys.Currency, Integer, String>toMap(ReadingTypeTranslationKeys.Currency::getCurrencyCode, c -> thesaurus.getFormat(c).format()))),
        UNIT(ReadingTypeFilter.ReadingTypeFields.UNIT.getName(),
                (thesaurus) -> Arrays.stream(ReadingTypeUnit.values()).filter(e -> e.getId() != 0)
                        .collect(Collectors.<ReadingTypeUnit, Integer, String>toMap(ReadingTypeUnit::getId, c -> thesaurus.getFormat(new ReadingTypeTranslationKeys.Unit(c)).format())));

        private final String name;
        private final Function<Thesaurus, Map<Integer, String>> values;

        ReadingTypeCodes(String name, Function<Thesaurus, Map<Integer, String>> values) {
            this.name = name;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public Map<Integer, String> getCodeInfo(Thesaurus thesaurus) {
            return values.apply(thesaurus);
        }
    }
}
