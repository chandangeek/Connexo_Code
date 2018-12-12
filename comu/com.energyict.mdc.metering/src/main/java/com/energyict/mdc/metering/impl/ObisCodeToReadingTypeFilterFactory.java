package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.TimeAttribute;

import com.energyict.obis.ObisCode;

import java.util.List;
import java.util.stream.Stream;

class ObisCodeToReadingTypeFilterFactory {


    static String createMRIDFilterFrom(ObisCode obisCode) {
        StringBuilder filter = new StringBuilder();
        Stream.of(ReadingTypeAttributeFilters.values()).forEach(f -> filter.append(f.getFilter(obisCode)));
        return filter.toString();
    }

    private enum ReadingTypeAttributeFilters {

        MACRO_PERIOD(){
            @Override
            String getFilter(ObisCode obisCode) {
                List<MacroPeriod> macroPeriodList = MacroPeriodMapping.getMacroPeriodListFor(obisCode);
                int[] macroPeriodArray = macroPeriodList.stream().mapToInt(MacroPeriod::getId).toArray();
                return this.createRegexFrom(macroPeriodArray) + REGEX_DOT;

            }
        },
        AGGREGATE() {
            @Override
            String getFilter(ObisCode obisCode) {
                Aggregate aggregate = AggregateMapping.getAggregateFor(obisCode, null);
                return String.valueOf(aggregate.getId()) + REGEX_DOT;
            }
        },
        MEASURING_PERIOD() {
            @Override
            String getFilter(ObisCode obisCode) {
                TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, null);
                return String.valueOf(timeAttribute.getId()) + REGEX_DOT;
            }
        },
        ACCUMULATION() {
            @Override
            String getFilter(ObisCode obisCode) {
                List<Accumulation> accumulationList = AccumulationMapping.getAccumulationListFor(obisCode);
                int[] accumulationArray = accumulationList.stream().mapToInt(Accumulation::getId).toArray();
                return this.createRegexFrom(accumulationArray) + REGEX_DOT;
            }
        },
        FLOW_DIRECTION {
            @Override
            String getFilter(ObisCode obisCode) {
                FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);
                return String.valueOf(flowDirection.getId()) + REGEX_DOT;

            }
        },
        COMMODITY {
            @Override
            String getFilter(ObisCode obisCode) {
                Commodity commodity = CommodityMapping.getCommodityFor(obisCode);
                return String.valueOf(commodity.getId()) + REGEX_DOT;
            }
        },
        MEASUREMENT_KIND {
            @Override
            String getFilter(ObisCode obisCode) {
                List<MeasurementKind> measurementKindList = MeasurementKindMapping.getMeasurementKindListFor(obisCode);
                int[] measurementKindArray = measurementKindList.stream().mapToInt(MeasurementKind::getId).toArray();
                return this.createRegexFrom(measurementKindArray) + REGEX_DOT;
            }
        },
        INTERHARMONICS {
            @Override
            String getFilter(ObisCode obisCode) {
                RationalNumber interHarmonic = InterHarmonicMapping.getInterHarmonicFor(obisCode);
                StringBuilder rationalNumberBuilder = new StringBuilder();
                rationalNumberBuilder.append(interHarmonic.getNumerator()).append(REGEX_DOT);
                rationalNumberBuilder.append(interHarmonic.getDenominator()).append(REGEX_DOT);
                return rationalNumberBuilder.toString();
            }
        },
        ARGUMENT {
            @Override
            String getFilter(ObisCode obisCode) {
                RationalNumber argument = RationalNumber.NOTAPPLICABLE;
                StringBuilder rationalNumberBuilder = new StringBuilder();
                rationalNumberBuilder.append(argument.getNumerator()).append(REGEX_DOT);
                rationalNumberBuilder.append(argument.getDenominator()).append(REGEX_DOT);
                return rationalNumberBuilder.toString();
            }
        },
        TIME_OF_USE {
            @Override
            String getFilter(ObisCode obisCode) {
                return String.valueOf(TimeOfUseMapping.getTimeOfUseFor(obisCode)) + REGEX_DOT;

            }
        },
        CRITICAL_PEAK_PERIOD {
            @Override
            String getFilter(ObisCode obisCode) {
                return REGEX_POSITIVE_INTEGER + REGEX_DOT;
            }
        },
        CONSUMPTION_TIER {
            @Override
            String getFilter(ObisCode obisCode) {
                return REGEX_POSITIVE_INTEGER + REGEX_DOT;
            }
        },
        PHASE {
            @Override
            String getFilter(ObisCode obisCode) {
                Phase phase = PhaseMapping.getPhaseFor(obisCode);
                return String.valueOf(phase.getId()) + REGEX_DOT;
            }
        },
        MULTIPLIER {
            @Override
            String getFilter(ObisCode obisCode) {
                return REGEX_ANY_INTEGER + REGEX_DOT;
            }
        },
        UNIT {
            @Override
            String getFilter(ObisCode obisCode) {
                return REGEX_POSITIVE_INTEGER + REGEX_DOT;
            }
        },
        CURRENCY {
            @Override
            String getFilter(ObisCode obisCode) {
                return REGEX_POSITIVE_INTEGER;
            }
        };

        private static String REGEX_START_GROUP = "(";
        private static String REGEX_END_GROUP = ")";
        private static String REGEX_EITHER = "|";
        private static String REGEX_DOT = "\\.";
        private static String REGEX_POSITIVE_INTEGER = "[0-9]+";
        private static String REGEX_ANY_INTEGER = "-?[0-9]+";


        abstract String getFilter(ObisCode obisCode);

        /**
         * (a1|a2|...|an) or a1
         * @param values - Non-empty list of integer values
         * @return regular expression to match all integer values in the array
         */
        String createRegexFrom(int[] values){
            if (values.length == 1)
                return String.valueOf(values[0]);

            StringBuilder regex = new StringBuilder();
            regex.append(REGEX_START_GROUP);
            int i = 1;
            for (int value : values){
                regex.append(value);
                if (i++ != values.length)
                    regex.append(REGEX_EITHER);
            }
            regex.append(REGEX_END_GROUP);
            return regex.toString();
        }
    }
}
