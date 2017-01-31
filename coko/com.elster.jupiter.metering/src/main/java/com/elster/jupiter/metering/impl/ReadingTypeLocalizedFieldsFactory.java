/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.elster.jupiter.util.units.Unit;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
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
                (thesaurus) -> Arrays.stream(MacroPeriod.values())
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        AGGREGATE(ReadingTypeFilter.ReadingTypeFields.AGGREAGTE.getName(),
                (thesaurus) -> Arrays.stream(Aggregate.values())
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.Aggregate(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        MEASUREMENT_PERIOD(ReadingTypeFilter.ReadingTypeFields.MEASUREMENT_PERIOD.getName(),
                (thesaurus) -> Arrays.stream(TimeAttribute.values())
                        .sorted((a,b) -> Integer.compare(a.getMinutes(),b.getMinutes()))
                        .sorted((a,b) -> Boolean.compare(a.getMinutes()==0 && a.getId()!=0,b.getMinutes()==0 && b.getId()!=0))
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MeasuringPeriod(c)).format()))
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        ACCUMULATION(ReadingTypeFilter.ReadingTypeFields.ACCUMULATION.getName(),
                (thesaurus) -> Arrays.stream(Accumulation.values())
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.AccumulationFields(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        FLOW_DIRECTION(ReadingTypeFilter.ReadingTypeFields.FLOW_DIRECTION.getName(),
                (thesaurus) -> Arrays.stream(FlowDirection.values())
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.FlowDirection(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        COMMODITY(ReadingTypeFilter.ReadingTypeFields.COMMODITY.getName(),
                (thesaurus) -> Arrays.stream(Commodity.values())
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.CommodityFields(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        MEASUREMENT_KIND(ReadingTypeFilter.ReadingTypeFields.MEASUREMENT_KIND.getName(),
                (thesaurus) -> Arrays.stream(MeasurementKind.values())
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MeasurementKind(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        INTERHARMONIC_NUMERATOR(ReadingTypeFilter.ReadingTypeFields.INTERHARMONIC_NUMERATOR.getName(),
                (thesaurus) -> Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll)),

        INTERHARMONIC_DENOMINATOR(ReadingTypeFilter.ReadingTypeFields.INTERHARMONIC_DENOMINATOR.getName(),
                (thesaurus) -> Stream.of(0, 1, 2)
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll)),

        ARGUMENT_NUMERATOR(ReadingTypeFilter.ReadingTypeFields.ARGUMENT_NUMERATOR.getName(),
                (thesaurus) -> Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 30, 45, 60, 12, 155, 240, 305, 360, 480, 720)
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll)),

        ARGUMENT_DENOMINATOR(ReadingTypeFilter.ReadingTypeFields.ARGUMENT_DENOMINATOR.getName(),
                (thesaurus) -> Stream.of(0, 1, 60, 120, 180, 240, 360)
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll)),

        TIME_OF_USE(ReadingTypeFilter.ReadingTypeFields.TIME_OF_USE.getName(),
                (thesaurus) -> Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8).collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll)),

        CPP(ReadingTypeFilter.ReadingTypeFields.CPP.getName(),
                (thesaurus) -> Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll)),

        CONSUMPTION_TIER(ReadingTypeFilter.ReadingTypeFields.CONSUMPTION_TIER.getName(),
                (thesaurus) -> Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll)),

        PHASES(ReadingTypeFilter.ReadingTypeFields.PHASES.getName(),
                (thesaurus) -> Arrays.stream(Phase.values()).distinct()
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.Phase(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        MULTIPLIER(ReadingTypeFilter.ReadingTypeFields.MULTIPLIER.getName(),
                (thesaurus) -> Arrays.stream(MetricMultiplier.values())
                        .map(e -> new CodeField(e.getMultiplier(), String.valueOf(e.getMultiplier()) + (e.getMultiplier() != 0 ? " (" + thesaurus.getFormat(new ReadingTypeTranslationKeys.Multiplier(e)).format() + ")" : "")))
                        .sorted((a,b) -> Integer.compare(a.code,b.code))
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        CURRENCY(ReadingTypeFilter.ReadingTypeFields.CURRENCY.getName(),
                (thesaurus) -> Arrays.stream(ReadingTypeTranslationKeys.Currency.values())
                        .map(e -> new CodeField(e.getCurrencyCode(), thesaurus.getFormat(e).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll)),

        UNIT(ReadingTypeFilter.ReadingTypeFields.UNIT.getName(),
                (thesaurus) -> Arrays.stream(ReadingTypeUnit.values())
                        .map(e -> new CodeField(e.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.UnitFields(e)).format() + (e.getUnit().equals(Unit.UNITLESS) ? "" : (" (" + thesaurus.getFormat(new ReadingTypeTranslationKeys.Unit(e)).format() + ")")))).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll));

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

    private static class CodeField implements Comparable<CodeField> {
        int code;
        String displayName;

        public CodeField(int code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        @Override
        public int compareTo(CodeField o) {
            if(code==0 || o.code==0){
                return Integer.compare(code,o.code);
            }
            return displayName.compareToIgnoreCase(o.displayName);
        }
    }
}
