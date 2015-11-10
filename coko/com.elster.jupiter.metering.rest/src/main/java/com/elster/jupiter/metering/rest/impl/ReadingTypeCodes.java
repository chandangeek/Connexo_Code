package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.*;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadingTypeCodes {

    public static List<ReadingTypeCodeInfo> getMacroPeriod() {
        return Arrays.stream(MacroPeriod.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());

    }

    public static List<ReadingTypeCodeInfo> getAggregate() {
        return Arrays.stream(Aggregate.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getMeasurementPeriod() {
        return Arrays.stream(TimeAttribute.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getAccumulation() {
        return Arrays.stream(Accumulation.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getFlowDirection() {
        return Arrays.stream(FlowDirection.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getCommodity() {
        return Arrays.stream(Commodity.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getMeasurementKind() {
        return Arrays.stream(MeasurementKind.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getInterHarmonicNumerator() {
        return Stream.of(1, 2, 3, 4, 5, 6, 7).map(ReadingTypeCodeInfo::new).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getInterHarmonicDenominator() {
        return Stream.of(1, 2).map(ReadingTypeCodeInfo::new).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getArgumentNumerator() {
        return Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 30, 45, 60, 12, 155, 240, 305, 360, 480, 720)
                .map(ReadingTypeCodeInfo::new).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getArgumentDenominator() {
        return Stream.of(1, 60, 120, 180, 240, 360).map(ReadingTypeCodeInfo::new).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getTou() {
        return Arrays.stream(TimeOfUse.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getCpp() {
        return Arrays.stream(CriticalPeakPeriod.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getConsumptionTier() {
        return Arrays.stream(ConsumptionTier.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getPhases() {
        return Arrays.stream(Phase.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getDescription())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getMultiplier() {
        return Arrays.stream(MetricMultiplier.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getSymbol())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getUnit() {
        return Arrays.stream(ReadingTypeUnit.values()).map(e -> new ReadingTypeCodeInfo(e.getId(), e.getSymbol())).collect(Collectors.toList());
    }

    public static List<ReadingTypeCodeInfo> getCurrency() {
        return Currency.getAvailableCurrencies().stream().map(e -> new ReadingTypeCodeInfo(e.getNumericCode(), e.getDisplayName())).collect(Collectors.toList());
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
