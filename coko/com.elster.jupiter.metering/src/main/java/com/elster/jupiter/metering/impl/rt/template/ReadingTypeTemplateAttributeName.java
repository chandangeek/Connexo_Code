package com.elster.jupiter.metering.impl.rt.template;

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

import java.util.Collections;
import java.util.Currency;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

public enum ReadingTypeTemplateAttributeName {
    MACRO_PERIOD {
        @Override
        public boolean canBeWildcard() {
            return true;
        }

        @Override
        public Set<MacroPeriod> getPossibleValues() {
            return EnumSet.of(
                    MacroPeriod.NOTAPPLICABLE,
                    MacroPeriod.BILLINGPERIOD,
                    MacroPeriod.DAILY,
                    MacroPeriod.MONTHLY
            );
        }

        @Override
        public Function<MacroPeriod, Integer> getValueToCodeConverter() {
            return MacroPeriod::getId;
        }

        @Override
        public Function<Integer, MacroPeriod> getCodeToValueConverter() {
            return MacroPeriod::get;
        }
    },
    AGGREGATE {
        @Override
        public Function<Integer, Aggregate> getCodeToValueConverter() {
            return Aggregate::get;
        }

        @Override
        public Function<Aggregate, Integer> getValueToCodeConverter() {
            return Aggregate::getId;
        }
    },
    TIME {
        @Override
        public boolean canBeWildcard() {
            return true;
        }

        @Override
        public Set<TimeAttribute> getPossibleValues() {
            return EnumSet.of(
                    TimeAttribute.NOTAPPLICABLE,
                    TimeAttribute.MINUTE10,
                    TimeAttribute.MINUTE15,
                    TimeAttribute.MINUTE1,
                    TimeAttribute.HOUR24,
                    TimeAttribute.MINUTE30,
                    TimeAttribute.MINUTE5,
                    TimeAttribute.MINUTE60
            );
        }

        @Override
        public Function<TimeAttribute, Integer> getValueToCodeConverter() {
            return TimeAttribute::getId;
        }

        @Override
        public Function<Integer, TimeAttribute> getCodeToValueConverter() {
            return TimeAttribute::get;
        }
    },
    ACCUMULATION {
        @Override
        public boolean canBeWildcard() {
            return true;
        }

        @Override
        public Set<Accumulation> getPossibleValues() {
            return EnumSet.of(
                    Accumulation.BULKQUANTITY,
                    Accumulation.DELTADELTA,
                    Accumulation.SUMMATION
            );
        }

        @Override
        public Function<Integer, Accumulation> getCodeToValueConverter() {
            return Accumulation::get;
        }

        @Override
        public Function<Accumulation, Integer> getValueToCodeConverter() {
            return Accumulation::getId;
        }
    },
    FLOW_DIRECTION {
        @Override
        public Function<Integer, FlowDirection> getCodeToValueConverter() {
            return FlowDirection::get;
        }

        @Override
        public Function<FlowDirection, Integer> getValueToCodeConverter() {
            return FlowDirection::getId;
        }
    },
    COMMODITY {
        @Override
        public Function<Integer, Commodity> getCodeToValueConverter() {
            return Commodity::get;
        }

        @Override
        public Function<Commodity, Integer> getValueToCodeConverter() {
            return Commodity::getId;
        }
    },
    MEASUREMENT_KIND {
        @Override
        public Function<Integer, MeasurementKind> getCodeToValueConverter() {
            return MeasurementKind::get;
        }

        @Override
        public Function<MeasurementKind, Integer> getValueToCodeConverter() {
            return MeasurementKind::getId;
        }
    },
    INTERHARMONIC_NUMERATOR {
        @Override
        public Function<Integer, Integer> getCodeToValueConverter() {
            return Function.identity();
        }

        @Override
        public Function<Integer, Integer> getValueToCodeConverter() {
            return Function.identity();
        }
    },
    INTERHARMONIC_DENOMINATOR {
        @Override
        public Function<Integer, Integer> getCodeToValueConverter() {
            return Function.identity();
        }

        @Override
        public Function<Integer, Integer> getValueToCodeConverter() {
            return Function.identity();
        }
    },
    ARGUMENT_NUMERATOR {
        @Override
        public Function<Integer, Integer> getCodeToValueConverter() {
            return Function.identity();
        }

        @Override
        public Function<Integer, Integer> getValueToCodeConverter() {
            return Function.identity();
        }
    },
    ARGUMENT_DENOMINATOR {
        @Override
        public Function<Integer, Integer> getCodeToValueConverter() {
            return Function.identity();
        }

        @Override
        public Function<Integer, Integer> getValueToCodeConverter() {
            return Function.identity();
        }
    },
    TIME_OF_USE {
        @Override
        public Set<Integer> getPossibleValues() {
            return IntStream.range(0, 49).collect(HashSet::new, HashSet::add, HashSet::addAll);
        }

        @Override
        public Function<Integer, Integer> getCodeToValueConverter() {
            return Function.identity();
        }

        @Override
        public Function<Integer, Integer> getValueToCodeConverter() {
            return Function.identity();
        }
    },
    CRITICAL_PEAK_PERIOD {
        @Override
        public Set<Integer> getPossibleValues() {
            return IntStream.range(0, 8).collect(HashSet::new, HashSet::add, HashSet::addAll);
        }

        @Override
        public Function<Integer, Integer> getCodeToValueConverter() {
            return Function.identity();
        }

        @Override
        public Function<Integer, Integer> getValueToCodeConverter() {
            return Function.identity();
        }
    },
    CONSUMPTION_TIER {
        @Override
        public Set<Integer> getPossibleValues() {
            return IntStream.range(0, 8).collect(HashSet::new, HashSet::add, HashSet::addAll);
        }

        @Override
        public Function<Integer, Integer> getCodeToValueConverter() {
            return Function.identity();
        }

        @Override
        public Function<Integer, Integer> getValueToCodeConverter() {
            return Function.identity();
        }
    },
    PHASE {
        @Override
        public Set<Phase> getPossibleValues() {
            return EnumSet.allOf(Phase.class);
        }

        @Override
        public Function<Integer, Phase> getCodeToValueConverter() {
            return Phase::get;
        }

        @Override
        public Function<Phase, Integer> getValueToCodeConverter() {
            return Phase::getId;
        }
    },
    METRIC_MULTIPLIER {
        @Override
        public boolean canBeWildcard() {
            return true;
        }

        @Override
        public Set<MetricMultiplier> getPossibleValues() {
            return EnumSet.of(
                    MetricMultiplier.ZERO,
                    MetricMultiplier.MICRO,
                    MetricMultiplier.MILLI,
                    MetricMultiplier.CENTI,
                    MetricMultiplier.DECI,
                    MetricMultiplier.DECA,
                    MetricMultiplier.HECTO,
                    MetricMultiplier.KILO,
                    MetricMultiplier.MEGA,
                    MetricMultiplier.GIGA,
                    MetricMultiplier.TERA,
                    MetricMultiplier.PETA
            );
        }

        @Override
        public Function<Integer, MetricMultiplier> getCodeToValueConverter() {
            return MetricMultiplier::with;
        }

        @Override
        public Function<MetricMultiplier, Integer> getValueToCodeConverter() {
            return MetricMultiplier::getMultiplier;
        }
    },
    UNIT_OF_MEASURE {
        @Override
        public Function<Integer, ReadingTypeUnit> getCodeToValueConverter() {
            return ReadingTypeUnit::get;
        }

        @Override
        public Function<ReadingTypeUnit, Integer> getValueToCodeConverter() {
            return ReadingTypeUnit::getId;
        }
    },
    CURRENCY {
        @Override
        public Function<Integer, Currency> getCodeToValueConverter() {
            return code -> Currency.getAvailableCurrencies()
                    .stream()
                    .filter(c -> c.getNumericCode() == code)
                    .findAny()
                    .orElse(null);
        }

        @Override
        public Function<Currency, Integer> getValueToCodeConverter() {
            return Currency::getNumericCode;
        }
    },;

    public boolean canBeWildcard() {
        return false;
    }

    public Set<?> getPossibleValues() {
        return Collections.emptySet();
    }

    public abstract Function<Integer, ?> getCodeToValueConverter();

    public abstract Function<?, Integer> getValueToCodeConverter();
}
