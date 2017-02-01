/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Currency;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@ProviderType
public class ReadingTypeMridFilter {
    private static String regexDot = "\\.";
    private static String positiveIntegerWildCard = "[0-9]+";
    private static String anyIntegerWildCard = "-?[0-9]+";

    private MacroPeriod macroPeriod = null;
    private Aggregate aggregate = null;
    private TimeAttribute measuringPeriod = null;
    private Accumulation accumulation = null;
    private FlowDirection flowDirection = null;
    private Commodity commodity = null;
    private MeasurementKind measurementKind = null;
    private RationalNumber interharmonic = null;
    private RationalNumber argument = null;
    private int tou = -1;
    private int cpp = -1;
    private int consumptionTier = -1;
    private Phase phases = null;
    private MetricMultiplier multiplier = null;
    private ReadingTypeUnit unit = null;
    private Currency currency = null;

    private enum Filters {
        MACRO_PERIOD() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.macroPeriod == null ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.macroPeriod.getId())) + regexDot;
            }
        },
        AGGREGATE() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.aggregate == null ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.aggregate.getId())) + regexDot;
            }
        },
        MEASUREINGPERIOD() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.measuringPeriod == null ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.measuringPeriod.getId())) + regexDot;
            }
        },
        ACCUMULATION() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.accumulation == null ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.accumulation.getId())) + regexDot;
            }
        },
        FLOWDIRECTION() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.flowDirection == null ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.flowDirection.getId())) + regexDot;
            }
        },
        COMMODITY() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.commodity == null ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.commodity.getId())) + regexDot;
            }
        },
        MEASUREMENTKIND() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.measurementKind == null ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.measurementKind.getId())) + regexDot;
            }
        },
        INTERHARMONICS() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                if (readingTypeMridFilter.interharmonic == null) {
                    return positiveIntegerWildCard + regexDot + positiveIntegerWildCard + regexDot;
                } else {
                    return readingTypeMridFilter.interharmonic.getNumerator() + regexDot + readingTypeMridFilter.interharmonic.getDenominator() + regexDot;
                }
            }
        },
        ARGUMENT() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                if (readingTypeMridFilter.argument == null) {
                    return positiveIntegerWildCard + regexDot + positiveIntegerWildCard + regexDot;
                } else {
                    return readingTypeMridFilter.argument.getNumerator() + regexDot + readingTypeMridFilter.argument.getDenominator() + regexDot;
                }
            }
        },
        TOU() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.tou < 0 ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.tou)) + regexDot;
            }
        },
        CPP() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.cpp < 0 ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.cpp)) + regexDot;
            }
        },
        CONSUMPTIONTIER() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.consumptionTier < 0 ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.consumptionTier)) + regexDot;
            }
        },
        PHASE() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.phases == null ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.phases.getId())) + regexDot;
            }
        },
        MULTIPLIER() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.multiplier == null ? anyIntegerWildCard : String.valueOf(readingTypeMridFilter.multiplier.getMultiplier())) + regexDot;
            }
        },
        UNIT() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.unit == null ? positiveIntegerWildCard : String.valueOf(readingTypeMridFilter.unit.getId())) + regexDot;
            }
        },
        CURRENCY() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                if(readingTypeMridFilter.currency == null){
                    return positiveIntegerWildCard;
                } else if(readingTypeMridFilter.currency.getNumericCode() == 999){
                    return "0";
                }
                return String.valueOf(readingTypeMridFilter.currency.getNumericCode());
            }
        };

        abstract String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter);
    }

    @SuppressWarnings("unused")
    public ReadingTypeMridFilter() {
    }

    public static ReadingTypeMridFilter fromTemplateReadingType(ReadingType readingType) {
        ReadingTypeMridFilter readingTypeMridFilter = new ReadingTypeMridFilter();
        readingTypeMridFilter.setMacroPeriod(readingType.getMacroPeriod());
        readingTypeMridFilter.setAggregate(readingType.getAggregate());
        readingTypeMridFilter.setMeasuringPeriod(readingType.getMeasuringPeriod());
        readingTypeMridFilter.setAccumulation(readingType.getAccumulation());
        readingTypeMridFilter.setFlowDirection(readingType.getFlowDirection());
        readingTypeMridFilter.setCommodity(readingType.getCommodity());
        readingTypeMridFilter.setMeasurementKind(readingType.getMeasurementKind());
        readingTypeMridFilter.setInterharmonic(readingType.getInterharmonic());
        readingTypeMridFilter.setArgument(readingType.getArgument());
        readingTypeMridFilter.setTou(readingType.getTou());
        readingTypeMridFilter.setCpp(readingType.getCpp());
        readingTypeMridFilter.setConsumptionTier(readingType.getConsumptionTier());
        readingTypeMridFilter.setPhases(readingType.getPhases());
        readingTypeMridFilter.setMultiplier(readingType.getMultiplier());
        readingTypeMridFilter.setUnit(readingType.getUnit());
        readingTypeMridFilter.setCurrency(readingType.getCurrency());
        return readingTypeMridFilter;
    }

    public ReadingTypeMridFilter setMacroPeriod(MacroPeriod macroPeriod) {
        this.macroPeriod = macroPeriod;
        return this;
    }

    public ReadingTypeMridFilter setAggregate(Aggregate aggregate) {
        this.aggregate = aggregate;
        return this;
    }

    public ReadingTypeMridFilter setMeasuringPeriod(TimeAttribute measuringPeriod) {
        this.measuringPeriod = measuringPeriod;
        return this;
    }

    public ReadingTypeMridFilter setAccumulation(Accumulation accumulation) {
        this.accumulation = accumulation;
        return this;
    }

    public ReadingTypeMridFilter setFlowDirection(FlowDirection flowDirection) {
        this.flowDirection = flowDirection;
        return this;
    }

    public ReadingTypeMridFilter setCommodity(Commodity commodity) {
        this.commodity = commodity;
        return this;
    }

    public ReadingTypeMridFilter setMeasurementKind(MeasurementKind measurementKind) {
        this.measurementKind = measurementKind;
        return this;
    }

    public ReadingTypeMridFilter setInterharmonic(RationalNumber interharmonic) {
        this.interharmonic = interharmonic;
        return this;
    }

    public ReadingTypeMridFilter setArgument(RationalNumber argument) {
        this.argument = argument;
        return this;
    }

    public ReadingTypeMridFilter setTou(int tou) {
        this.tou = tou;
        return this;
    }

    public ReadingTypeMridFilter setCpp(int cpp) {
        this.cpp = cpp;
        return this;
    }

    public ReadingTypeMridFilter setConsumptionTier(int consumptionTier) {
        this.consumptionTier = consumptionTier;
        return this;
    }

    public ReadingTypeMridFilter setPhases(Phase phases) {
        this.phases = phases;
        return this;
    }

    public ReadingTypeMridFilter setMultiplier(MetricMultiplier multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public ReadingTypeMridFilter setUnit(ReadingTypeUnit unit) {
        this.unit = unit;
        return this;
    }

    public ReadingTypeMridFilter setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public ReadingTypeMridFilter anyMacroPeriod() {
        this.macroPeriod = null;
        return this;
    }

    public ReadingTypeMridFilter anyAggregate() {
        this.aggregate = null;
        return this;
    }

    public ReadingTypeMridFilter anyMeasuringPeriod() {
        this.measuringPeriod = null;
        return this;
    }

    public ReadingTypeMridFilter anyAccumulation() {
        this.accumulation = null;
        return this;
    }

    public ReadingTypeMridFilter anyFlowDirection() {
        this.flowDirection = null;
        return this;
    }

    public ReadingTypeMridFilter anyCommodity() {
        this.commodity = null;
        return this;
    }

    public ReadingTypeMridFilter anyMeasurementKind() {
        this.measurementKind = null;
        return this;
    }

    public ReadingTypeMridFilter anyInterharmonic() {
        this.interharmonic = null;
        return this;
    }

    public ReadingTypeMridFilter anyArgument() {
        this.argument = null;
        return this;
    }

    public ReadingTypeMridFilter anyTou() {
        this.tou = -1;
        return this;
    }

    public ReadingTypeMridFilter anyCpp() {
        this.cpp = -1;
        return this;
    }

    public ReadingTypeMridFilter anyConsumptionTier() {
        this.consumptionTier = -1;
        return this;
    }

    public ReadingTypeMridFilter anyPhases() {
        this.phases = null;
        return this;
    }

    public ReadingTypeMridFilter anyMultiplier() {
        this.multiplier = null;
        return this;
    }

    public ReadingTypeMridFilter anyUnit() {
        this.unit = null;
        return this;
    }

    public ReadingTypeMridFilter anyCurrency() {
        this.currency = null;
        return this;
    }

    public Condition getFilterCondition() {
        return where("mRID").matches(getRegex(), "");
    }

    public String getRegex() {
        StringBuilder filter = new StringBuilder();
        Stream.of(Filters.values()).forEach(f -> filter.append(f.getFilterCriteria(this)));
        return filter.toString();
    }
}
