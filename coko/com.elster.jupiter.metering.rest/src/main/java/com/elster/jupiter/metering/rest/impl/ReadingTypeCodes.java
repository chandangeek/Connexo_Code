package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.*;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ReadingTypeCodes {
    MACRO_PERIOD("macroPeriod",
            () -> Arrays.stream(MacroPeriod.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    AGGREAGTE("aggregate",
            () -> Arrays.stream(Aggregate.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    MEASUREMENT_PERIOD("measurementPeriod",
            () -> Arrays.stream(TimeAttribute.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    ACCUMULATION("accumulation",
            () -> Arrays.stream(Accumulation.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    FLOW_DIRECTION("flowDirection",
            () -> Arrays.stream(FlowDirection.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    COMMODITY("commodity",
            () -> Arrays.stream(Commodity.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    MEASUREMENT_KIND("measurementKind",
            () -> Arrays.stream(MeasurementKind.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    INTERHARMONIC_NUMERATOR("interHarmonicNumerator",
            () -> Stream.of(1, 2, 3, 4, 5, 6, 7).map(ReadingTypeCodeInfo::new).collect(Collectors.toList())),
    INTERHARMONIC_DENOMINATOR("interHarmonicDenominator",
            () -> Stream.of(1, 2).map(ReadingTypeCodeInfo::new).collect(Collectors.toList())),
    ARGUMENT_NUMERATOR("argumentNumerator",
            () -> Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 30, 45, 60, 12, 155, 240, 305, 360, 480, 720)
                    .map(ReadingTypeCodeInfo::new).collect(Collectors.toList())),
    ARGUMENT_DENOMINATOR("argumentDenominator",
            () -> Stream.of(1, 60, 120, 180, 240, 360).map(ReadingTypeCodeInfo::new).collect(Collectors.toList())),
    TIME_OF_USE("timeOfUse",
            () -> Arrays.stream(TimeOfUse.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    CPP("criticalPeakPeriod",
            () -> Arrays.stream(CriticalPeakPeriod.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    CONSUMPTION_TIER("consumptionTier",
            () -> Arrays.stream(ConsumptionTier.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    PHASES("phases",
            () -> Arrays.stream(Phase.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).filter(e -> e.code!=0).collect(Collectors.toList())),
    MULTIPLIER("multiplier",
            () -> Arrays.stream(MetricMultiplier.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getSymbol())).filter(e -> e.code!=0).collect(Collectors.toList())),
    UNIT("unit",
            () -> Arrays.stream(ReadingTypeUnit.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getSymbol())).filter(e -> e.code!=0).collect(Collectors.toList())),
    CURRENCY("currency",
            () -> Currency.getAvailableCurrencies().stream().map(e -> new ReadingTypeCodeInfo(e.getNumericCode(), e.getDisplayName())).filter(e -> e.code!=0).collect(Collectors.toList()));

    private final String name;
    private final Supplier<List<ReadingTypeCodeInfo>> values;

    ReadingTypeCodes(String name, Supplier<List<ReadingTypeCodeInfo>> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<ReadingTypeCodeInfo> getCodeInfo() {
        return values.get();
    }

    private enum TimeOfUse {
        A(1, "touA"),
        B(2, "touB"),
        C(3, "touC"),
        D(4, "touD"),
        E(5, "touE");

        private int id;
        private String description;

        TimeOfUse(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    private enum CriticalPeakPeriod {
        A(1, "cppA"),
        B(2, "cppB"),
        C(3, "cppC"),
        D(4, "cppD"),
        E(5, "cppE"),
        F(6, "cppF"),
        G(7, "cppG");

        private int id;
        private String description;

        CriticalPeakPeriod(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    private enum ConsumptionTier {
        CONSUMPTION_TIER_1(1, "consumptionTier1"),
        CONSUMPTION_TIER_2(2, "consumptionTier2"),
        CONSUMPTION_TIER_3(3, "consumptionTier3"),
        CONSUMPTION_TIER_4(4, "consumptionTier4"),
        CONSUMPTION_TIER_5(5, "consumptionTier5"),
        CONSUMPTION_TIER_6(6, "consumptionTier6"),
        CONSUMPTION_TIER_7(7, "consumptionTier7");

        private int id;
        private String description;

        ConsumptionTier(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }
}
