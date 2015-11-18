package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cbo.*;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Currency;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@ProviderType
public class ReadingTypeMridFilter {
    private static String regexDot = "\\.";
    private static String wildCard = "[0-9]+";

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
                return (readingTypeMridFilter.macroPeriod == null ? wildCard : String.valueOf(readingTypeMridFilter.macroPeriod.getId())) + regexDot;
            }
        },
        AGGREGATE() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.aggregate == null ? wildCard : String.valueOf(readingTypeMridFilter.aggregate.getId())) + regexDot;
            }
        },
        MEASUREINGPERIOD() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.measuringPeriod == null ? wildCard : String.valueOf(readingTypeMridFilter.measuringPeriod.getId())) + regexDot;
            }
        },
        ACCUMULATION() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.accumulation == null ? wildCard : String.valueOf(readingTypeMridFilter.accumulation.getId())) + regexDot;
            }
        },
        FLOWDIRECTION() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.flowDirection == null ? wildCard : String.valueOf(readingTypeMridFilter.flowDirection.getId())) + regexDot;
            }
        },
        COMMODITY() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.commodity == null ? wildCard : String.valueOf(readingTypeMridFilter.commodity.getId())) + regexDot;
            }
        },
        MEASUREMENTKIND() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.measurementKind == null ? wildCard : String.valueOf(readingTypeMridFilter.measurementKind.getId())) + regexDot;
            }
        },
        INTERHARMONICS() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                if (readingTypeMridFilter.interharmonic == null) {
                    return wildCard + regexDot + wildCard + regexDot;
                } else {
                    return readingTypeMridFilter.interharmonic.getNumerator() + regexDot + readingTypeMridFilter.interharmonic.getDenominator() + regexDot;
                }
            }
        },
        ARGUMENT() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                if (readingTypeMridFilter.argument == null) {
                    return wildCard + regexDot + wildCard + regexDot;
                } else {
                    return readingTypeMridFilter.argument.getNumerator() + regexDot + readingTypeMridFilter.argument.getDenominator() + regexDot;
                }
            }
        },
        TOU() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.tou < 0 ? wildCard : String.valueOf(readingTypeMridFilter.tou)) + regexDot;
            }
        },
        CPP() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.cpp < 0 ? wildCard : String.valueOf(readingTypeMridFilter.cpp)) + regexDot;
            }
        },
        CONSUMPTIONTIER() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.consumptionTier < 0 ? wildCard : String.valueOf(readingTypeMridFilter.consumptionTier)) + regexDot;
            }
        },
        PHASE() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.phases == null ? wildCard : String.valueOf(readingTypeMridFilter.phases.getId())) + regexDot;
            }
        },
        MULTIPLIER() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.multiplier == null ? wildCard : String.valueOf(readingTypeMridFilter.multiplier.getId())) + regexDot;
            }
        },
        UNIT() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                return (readingTypeMridFilter.unit == null ? wildCard : String.valueOf(readingTypeMridFilter.unit.getId())) + regexDot;
            }
        },
        CURRENCY() {
            @Override
            String getFilterCriteria(ReadingTypeMridFilter readingTypeMridFilter) {
                if(readingTypeMridFilter.currency == null){
                    return wildCard;
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

    public Condition getFilterCondition() {
        StringBuilder filter = new StringBuilder();
        Stream.of(Filters.values()).forEach(f -> filter.append(f.getFilterCriteria(this)));
        return where("mRID").matches(filter.toString(), "");
    }
}
