/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.*;
import com.elster.jupiter.metering.ReadingTypeFieldsFactory;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Unit;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ReadingTypeGroupLocalizedFieldsFactory implements ReadingTypeFieldsFactory {

    final Thesaurus thesaurus;
    final Integer filterBy;

    Map<Commodity, EnumSet<MeasurementKind>> mapCommodity2MeasurementKind = new LinkedHashMap<>();

    public ReadingTypeGroupLocalizedFieldsFactory(Thesaurus thesaurus, Integer filterBy) {
        this.thesaurus = thesaurus;
        this.filterBy = filterBy;
    }

    public Map<Integer, String> getCodeFields(String field) {

        ReadingTypeFilter.ReadingTypeFields readingTypeCodes = Arrays.stream(ReadingTypeFilter.ReadingTypeFields.values())
                .filter(e -> e.getName().equals(field))
                .findFirst().orElseThrow(IllegalArgumentException::new);

        Map<Integer, String> values = null;
        switch (readingTypeCodes) {
            case COMMODITY:
                values = EnumSet.of(
                        Commodity.NOTAPPLICABLE,
                        Commodity.ELECTRICITY_PRIMARY_METERED,
                        // Commodity.ELECTRICITY_SECONDARY_METERED,
                        Commodity.NATURALGAS,
                        Commodity.POTABLEWATER,
                        Commodity.DEVICE)
                        .stream()
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.CommodityFields(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll);
                values.put(Commodity.ELECTRICITY_PRIMARY_METERED.getId(),
                        thesaurus.getString("readingType.commodity.electricity", "Electricity"));
                break;
            case MEASUREMENT_KIND: {

                Commodity commodity = this.filterBy != null ? Commodity.get(this.filterBy) : null;
                values = Arrays.stream(MeasurementKindByCommodity.values())
                        .filter(c -> c.name().compareToIgnoreCase(commodity.name()) == 0)
                        .findFirst().orElse(MeasurementKindByCommodity.NOTAPPLICABLE)
                        .getValues()
                        .stream()
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MeasurementKind(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll);
                break;
            }

            case UNIT: {
                MeasurementKind kind = this.filterBy != null ? MeasurementKind.get(this.filterBy) : null;
                if (kind != null)
                {
                    values = Arrays.stream(UnitByMeasurementKind.values())
                            .filter(mk -> mk.name().compareToIgnoreCase(kind.name()) == 0)
                            .findFirst().orElse(UnitByMeasurementKind.NOTAPPLICABLE)
                            .getValues()
                            .stream()
                            .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.UnitFields(c)).format() + (c.getUnit().equals(Unit.UNITLESS) ? "" : (" (" + thesaurus.getFormat(new ReadingTypeTranslationKeys.Unit(c)).format() + ")")))).sorted()
                            .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll);
                }
                break;
            }
            case FLOW_DIRECTION: {
                Commodity commodity = this.filterBy != null ? Commodity.get(this.filterBy) : null;
                values = Arrays.stream(FlowDirectionByCommodity.values())
                        .filter(c -> c.name().compareToIgnoreCase(commodity.name()) == 0)
                        .findFirst().orElse(FlowDirectionByCommodity.NOTAPPLICABLE)
                        .getValues()
                        .stream()
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.FlowDirection(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll);
                break;
            }

            case MACRO_PERIOD: {
                // Commodity commodity = this.filterBy != null ? Commodity.get(this.filterBy) : null;  // initialy was only for Electricity
                values = new LinkedHashMap();
                values.put(MacroPeriod.NOTAPPLICABLE.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(MacroPeriod.NOTAPPLICABLE)).format());
//                if (commodity == Commodity.ELECTRICITY_PRIMARY_METERED) {  // initialy was only for Electricity
//                    values.put(0x10000,
//                            thesaurus.getString("readingType.macroperiod.less1day", "Interval < 1 day"));
//                }
                values.put(0x10000,
                            thesaurus.getString("readingType.macroperiod.less1day", "Interval < 1 day"));
                values.put(MacroPeriod.BILLINGPERIOD.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(MacroPeriod.BILLINGPERIOD)).format());
                values.put(MacroPeriod.DAILY.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(MacroPeriod.DAILY)).format());
                values.put(MacroPeriod.MONTHLY.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(MacroPeriod.MONTHLY)).format());
                values.put(MacroPeriod.SEASONAL.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(MacroPeriod.SEASONAL)).format());
                values.put(MacroPeriod.WEEKLYS.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(MacroPeriod.WEEKLYS)).format());
                values.put(MacroPeriod.SPECIFIEDPERIOD.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(MacroPeriod.SPECIFIEDPERIOD)).format());
                values.put(MacroPeriod.YEARLY.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MacroPeriod(MacroPeriod.YEARLY)).format());
                break;
            }

            case AGGREAGTE: {
                values = Arrays.stream(Aggregate.values())
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.Aggregate(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll);
                break;
            }

            case ACCUMULATION: {
                values = Arrays.stream(AccumulationByMacroPeriod.values())
                        .findFirst().orElse(AccumulationByMacroPeriod.NOTAPPLICABLE)
                        .getValues()
                        .stream()
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.AccumulationFields(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll);
                break;
            }

            case MEASUREMENT_PERIOD: {
                values = Arrays.stream(TimeAttribute.values())
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.MeasuringPeriod(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll);
                break;
            }

            case MULTIPLIER: {
                values = Arrays.stream(MetricMultiplier.values())
                        .map(c -> new CodeField(c.getMultiplier(), String.valueOf(c.getMultiplier()) + (c.getMultiplier() != 0 ? " (" + thesaurus.getFormat(new ReadingTypeTranslationKeys.Multiplier(c)).format() + ")" : "")))
                        .sorted((a, b) -> Integer.compare(a.code, b.code))
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll);
                break;
            }

            case PHASES: {
                values = Arrays.stream(Phase.values())
                        .map(c -> new CodeField(c.getId(), thesaurus.getFormat(new ReadingTypeTranslationKeys.Phase(c)).format())).sorted()
                        .collect(LinkedHashMap::new, (map, element) -> map.put(element.code, element.displayName), Map::putAll);
                break;
            }

            case TIME_OF_USE: {
                values = Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll);
                break;
            }

            case CPP: {
                values = Stream.of(0, 1, 2, 3, 4, 5, 6, 7).collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll);
                break;
            }

            case CONSUMPTION_TIER: {
                values = Stream.of(0, 1, 2, 3, 4, 5, 6, 7).collect(LinkedHashMap::new, (map, element) -> map.put(element, String.valueOf(element)), Map::putAll);
                break;
            }
        }
        if (values == null)
            throw new IllegalArgumentException();
        return values;
    }

    enum MeasurementKindByCommodity {
        NOTAPPLICABLE(EnumSet.of(
                MeasurementKind.NOTAPPLICABLE)),
        ELECTRICITY_PRIMARY_METERED(EnumSet.of(
                MeasurementKind.NOTAPPLICABLE,
                MeasurementKind.CURRENT,
                MeasurementKind.CURRENTANGLE,
                MeasurementKind.DEMAND,
                MeasurementKind.ENERGY,
                MeasurementKind.POWER,
                MeasurementKind.POWERFACTOR,
                MeasurementKind.VOLTAGE
        )),
        //        ELECTRICITY_SECONDARY_METERED(EnumSet.of(
//                MeasurementKind.NOTAPPLICABLE,
//                MeasurementKind.CURRENT,
//                MeasurementKind.CURRENTANGLE,
//                MeasurementKind.DEMAND,
//                MeasurementKind.ENERGY,
//                MeasurementKind.POWER,
//                MeasurementKind.POWERFACTOR,
//                MeasurementKind.VOLTAGE
//        )),
        NATURALGAS(EnumSet.of(
                MeasurementKind.NOTAPPLICABLE,
                MeasurementKind.VOLUME,
                MeasurementKind.ENERGY,
                MeasurementKind.TEMPERATURE,
                MeasurementKind.VOLUMETRICFLOW
        )),

        POTABLEWATER(EnumSet.of(
                MeasurementKind.NOTAPPLICABLE,
                MeasurementKind.VOLUME,
                MeasurementKind.TEMPERATURE,
                MeasurementKind.VOLUMETRICFLOW
        )),
        DEVICE(EnumSet.of(
                MeasurementKind.NOTAPPLICABLE,
                MeasurementKind.BATTERYVOLTAGE,
                MeasurementKind.SIGNALSTRENGTH,
                MeasurementKind.TEMPERATURE,
                MeasurementKind.ASSETNUMBER
        ));

        EnumSet<MeasurementKind> set;

        MeasurementKindByCommodity(EnumSet<MeasurementKind> set) {
            this.set = set;
        }

        public EnumSet<MeasurementKind> getValues() {
            return set;
        }
    }

    ;

    enum UnitByMeasurementKind {
        NOTAPPLICABLE(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE)),
        CURRENT(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.AMPERE)),

        CURRENTANGLE(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.RADIAN,
                ReadingTypeUnit.DEGREES)),

        DEMAND(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.WATT)),

        ENERGY(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.WATTHOUR,
                //ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR,
                //ReadingTypeUnit.VOLTAMPEREREACTIVE,
                ReadingTypeUnit.JOULE,
                ReadingTypeUnit.BRITISHTHERMALUNIT)),

        POWER(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.WATT)),

        POWERFACTOR(EnumSet.of(ReadingTypeUnit.NOTAPPLICABLE)),

        VOLTAGE(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.VOLT)),

        BATTERYVOLTAGE(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.VOLT)),

        SIGNALSTRENGTH(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.BEL,
                ReadingTypeUnit.BELMILLIWATT)),

        VOLUME(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.CUBICMETER,
                ReadingTypeUnit.CUBICMETERCOMPENSATED,
                ReadingTypeUnit.CUBICMETERPERHOUR,
                ReadingTypeUnit.CUBICMETERPERHOURCOMPENSATED,
                ReadingTypeUnit.LITRE,
                ReadingTypeUnit.LITRECOMPENSATED,
                ReadingTypeUnit.PASCAL,
                ReadingTypeUnit.WATTHOURPERCUBICMETER,
                ReadingTypeUnit.CUBICFEET,
                ReadingTypeUnit.CUBICFEETCOMPENSATED,
                ReadingTypeUnit.CUBICFEETUNCOMPENSATED,
                ReadingTypeUnit.CUBICFEETPERHOUR,
                ReadingTypeUnit.CUBICFEETCOMPENSATEDPERHOUR,
                ReadingTypeUnit.CUBICFEETUNCOMPENSATEDPERHHOUR,
                ReadingTypeUnit.CUBICYARD)),

        VOLUMETRICFLOW(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.CUBICMETERPERHOUR,
                ReadingTypeUnit.CUBICFEETCOMPENSATEDPERHOUR,
                ReadingTypeUnit.CUBICFEETUNCOMPENSATEDPERHHOUR,
                ReadingTypeUnit.CUBICMETERPERSECOND)),

        TEMPERATURE(EnumSet.of(
                ReadingTypeUnit.NOTAPPLICABLE,
                ReadingTypeUnit.KELVIN,
                ReadingTypeUnit.DEGREESCELSIUS,
                ReadingTypeUnit.DEGREESFAHRENHEIT)),;

        EnumSet<ReadingTypeUnit> set;

        UnitByMeasurementKind(EnumSet<ReadingTypeUnit> set) {
            this.set = set;
        }

        public EnumSet<ReadingTypeUnit> getValues() {
            return set;
        }
    }

    ;

    enum FlowDirectionByCommodity {
        NOTAPPLICABLE(EnumSet.of(
                FlowDirection.NOTAPPLICABLE)),
        ELECTRICITY_PRIMARY_METERED(EnumSet.complementOf(EnumSet.range(FlowDirection.Q1PLUSQ2, FlowDirection.Q3MINUSQ2))),
        //ELECTRICITY_SECONDARY_METERED(EnumSet.complementOf(EnumSet.range(FlowDirection.Q1PLUSQ2, FlowDirection.Q3MINUSQ2))),
        NATURALGAS(EnumSet.of(FlowDirection.NOTAPPLICABLE, FlowDirection.FORWARD, FlowDirection.REVERSE)),
        POTABLEWATER(EnumSet.of(FlowDirection.NOTAPPLICABLE, FlowDirection.FORWARD, FlowDirection.REVERSE));

        EnumSet<FlowDirection> set;

        FlowDirectionByCommodity(EnumSet<FlowDirection> set) {
            this.set = set;
        }

        public EnumSet<FlowDirection> getValues() {
            return set;
        }
    }

    ;

    enum AccumulationByMacroPeriod {
        NOTAPPLICABLE(EnumSet.of(
                Accumulation.NOTAPPLICABLE,
                Accumulation.BULKQUANTITY,
                Accumulation.DELTADELTA,
                Accumulation.SUMMATION,
                Accumulation.INSTANTANEOUS));


        EnumSet<Accumulation> set;

        AccumulationByMacroPeriod(EnumSet<Accumulation> set) {
            this.set = set;
        }

        public EnumSet<Accumulation> getValues() {
            return set;
        }
    }

    ;

    private static class CodeField implements Comparable<CodeField> {
        int code;
        String displayName;

        public CodeField(int code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        @Override
        public int compareTo(CodeField o) {
            if (code == 0 || o.code == 0) {
                return Integer.compare(code, o.code);
            }
            return displayName.compareToIgnoreCase(o.displayName);
        }
    }
}
