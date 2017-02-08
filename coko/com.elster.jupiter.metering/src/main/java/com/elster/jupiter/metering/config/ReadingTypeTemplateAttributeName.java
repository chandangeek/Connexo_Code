/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

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
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.impl.ReadingTypeTranslationKeys;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;

import aQute.bnd.annotation.ProviderType;

import java.util.Collections;
import java.util.Currency;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

@ProviderType
public enum ReadingTypeTemplateAttributeName {
    MACRO_PERIOD(new ReadingTypeAttribute<MacroPeriod>(MacroPeriod.class) {
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

        @Override
        public Function<MacroPeriod, TranslationKey> getTranslationProvider() {
            return ReadingTypeTranslationKeys.MacroPeriod::new;
        }

        @Override
        public Function<ReadingType, MacroPeriod> getReadingTypeAttributeValue() {
            return ReadingType::getMacroPeriod;
        }
    }),
    AGGREGATE(new ReadingTypeAttribute<Aggregate>(Aggregate.class) {
        @Override
        public Function<Integer, Aggregate> getCodeToValueConverter() {
            return Aggregate::get;
        }

        @Override
        public Function<Aggregate, Integer> getValueToCodeConverter() {
            return Aggregate::getId;
        }

        @Override
        public Function<Aggregate, TranslationKey> getTranslationProvider() {
            return ReadingTypeTranslationKeys.Aggregate::new;
        }

        @Override
        public Function<ReadingType, Aggregate> getReadingTypeAttributeValue() {
            return ReadingType::getAggregate;
        }
    }),
    TIME(new ReadingTypeAttribute<TimeAttribute>(TimeAttribute.class) {
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
        public Function<TimeAttribute, TranslationKey> getTranslationProvider() {
            return ReadingTypeTranslationKeys.MeasuringPeriod::new;
        }

        @Override
        public Function<ReadingType, TimeAttribute> getReadingTypeAttributeValue() {
            return ReadingType::getMeasuringPeriod;
        }

        @Override
        public Function<Integer, TimeAttribute> getCodeToValueConverter() {
            return TimeAttribute::get;
        }
    }),
    ACCUMULATION(new ReadingTypeAttribute<Accumulation>(Accumulation.class) {
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

        @Override
        public Function<Accumulation, TranslationKey> getTranslationProvider() {
            return ReadingTypeTranslationKeys.AccumulationFields::new;
        }

        @Override
        public Function<ReadingType, Accumulation> getReadingTypeAttributeValue() {
            return ReadingType::getAccumulation;
        }
    }),
    FLOW_DIRECTION(new ReadingTypeAttribute<FlowDirection>(FlowDirection.class) {
        @Override
        public Function<Integer, FlowDirection> getCodeToValueConverter() {
            return FlowDirection::get;
        }

        @Override
        public Function<FlowDirection, Integer> getValueToCodeConverter() {
            return FlowDirection::getId;
        }

        @Override
        public Function<FlowDirection, TranslationKey> getTranslationProvider() {
            return ReadingTypeTranslationKeys.FlowDirection::new;
        }

        @Override
        public Function<ReadingType, FlowDirection> getReadingTypeAttributeValue() {
            return ReadingType::getFlowDirection;
        }
    }),
    COMMODITY(new ReadingTypeAttribute<Commodity>(Commodity.class) {
        @Override
        public Function<Integer, Commodity> getCodeToValueConverter() {
            return Commodity::get;
        }

        @Override
        public Function<Commodity, Integer> getValueToCodeConverter() {
            return Commodity::getId;
        }

        @Override
        public Function<Commodity, TranslationKey> getTranslationProvider() {
            return ReadingTypeTranslationKeys.CommodityFields::new;
        }

        @Override
        public Function<ReadingType, Commodity> getReadingTypeAttributeValue() {
            return ReadingType::getCommodity;
        }
    }),
    MEASUREMENT_KIND(new ReadingTypeAttribute<MeasurementKind>(MeasurementKind.class) {
        @Override
        public Function<Integer, MeasurementKind> getCodeToValueConverter() {
            return MeasurementKind::get;
        }

        @Override
        public Function<MeasurementKind, Integer> getValueToCodeConverter() {
            return MeasurementKind::getId;
        }

        @Override
        public Function<MeasurementKind, TranslationKey> getTranslationProvider() {
            return ReadingTypeTranslationKeys.MeasurementKind::new;
        }

        @Override
        public Function<ReadingType, MeasurementKind> getReadingTypeAttributeValue() {
            return ReadingType::getMeasurementKind;
        }
    }),
    INTERHARMONIC_NUMERATOR(new IntegerReadingTypeAttribute() {
        @Override
        public Function<ReadingType, Integer> getReadingTypeAttributeValue() {
            return rt -> Long.valueOf(rt.getInterharmonic().getNumerator()).intValue();
        }
    }),
    INTERHARMONIC_DENOMINATOR(new IntegerReadingTypeAttribute() {
        @Override
        public Function<ReadingType, Integer> getReadingTypeAttributeValue() {
            return rt -> Long.valueOf(rt.getInterharmonic().getDenominator()).intValue();
        }
    }),
    ARGUMENT_NUMERATOR(new IntegerReadingTypeAttribute() {
        @Override
        public Function<ReadingType, Integer> getReadingTypeAttributeValue() {
            return rt -> Long.valueOf(rt.getArgument().getNumerator()).intValue();
        }
    }),
    ARGUMENT_DENOMINATOR(new IntegerReadingTypeAttribute() {
        @Override
        public Function<ReadingType, Integer> getReadingTypeAttributeValue() {
            return rt -> Long.valueOf(rt.getArgument().getDenominator()).intValue();
        }
    }),
    TIME_OF_USE(new IntegerReadingTypeAttribute() {
        @Override
        public Set<Integer> getPossibleValues() {
            return IntStream.range(0, 49).collect(HashSet::new, HashSet::add, HashSet::addAll);
        }

        @Override
        public Function<ReadingType, Integer> getReadingTypeAttributeValue() {
            return ReadingType::getTou;
        }
    }),
    CRITICAL_PEAK_PERIOD(new IntegerReadingTypeAttribute() {
        @Override
        public Set<Integer> getPossibleValues() {
            return IntStream.range(0, 49).collect(HashSet::new, HashSet::add, HashSet::addAll);
        }

        @Override
        public Function<ReadingType, Integer> getReadingTypeAttributeValue() {
            return ReadingType::getCpp;
        }
    }),
    CONSUMPTION_TIER(new IntegerReadingTypeAttribute() {
        @Override
        public Set<Integer> getPossibleValues() {
            return IntStream.range(0, 49).collect(HashSet::new, HashSet::add, HashSet::addAll);
        }

        @Override
        public Function<ReadingType, Integer> getReadingTypeAttributeValue() {
            return ReadingType::getConsumptionTier;
        }
    }),
    PHASE(new ReadingTypeAttribute<Phase>(Phase.class) {
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

        @Override
        public Function<Phase, TranslationKey> getTranslationProvider() {
            return ReadingTypeTranslationKeys.Phase::new;
        }

        @Override
        public Function<ReadingType, Phase> getReadingTypeAttributeValue() {
            return ReadingType::getPhases;
        }
    }),
    METRIC_MULTIPLIER(new ReadingTypeAttribute<MetricMultiplier>(MetricMultiplier.class) {
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

        @Override
        public Function<MetricMultiplier, TranslationKey> getTranslationProvider() {
            return value -> new SimpleTranslationKey(getType().getName() + "." + value.name(), value.getSymbol());
        }

        @Override
        public Function<ReadingType, MetricMultiplier> getReadingTypeAttributeValue() {
            return ReadingType::getMultiplier;
        }
    }),
    UNIT_OF_MEASURE(new ReadingTypeAttribute<ReadingTypeUnit>(ReadingTypeUnit.class) {
        @Override
        public Function<Integer, ReadingTypeUnit> getCodeToValueConverter() {
            return ReadingTypeUnit::get;
        }

        @Override
        public Function<ReadingTypeUnit, Integer> getValueToCodeConverter() {
            return ReadingTypeUnit::getId;
        }

        @Override
        public Function<ReadingTypeUnit, TranslationKey> getTranslationProvider() {
            return ReadingTypeTranslationKeys.Unit::new;
        }

        @Override
        public Function<ReadingType, ReadingTypeUnit> getReadingTypeAttributeValue() {
            return ReadingType::getUnit;
        }
    }),
    CURRENCY(new ReadingTypeAttribute<Currency>(Currency.class) {
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
            return currency -> currency.getNumericCode() == 999 ? 0 : currency.getNumericCode();
        }

        @Override
        public Function<Currency, TranslationKey> getTranslationProvider() {
            return value -> new SimpleTranslationKey("readingType.currency." + value,
                    ReadingTypeTranslationKeys.Currency.getCurrencyDefaultFormat(value));
        }

        @Override
        public Function<ReadingType, Currency> getReadingTypeAttributeValue() {
            return ReadingType::getCurrency;
        }
    }),;

    private final ReadingTypeAttribute<?> attribute;

    ReadingTypeTemplateAttributeName(ReadingTypeAttribute<?> attribute) {
        this.attribute = attribute;
    }

    public ReadingTypeAttribute<?> getDefinition() {
        return this.attribute;
    }

    public static <T> int getReadingTypeAttributeCode(ReadingTypeAttribute<T> definition, ReadingType candidate) {
        return getCodeFromAttributeValue(definition, definition.getReadingTypeAttributeValue().apply(candidate));
    }

    @SuppressWarnings("unchecked")
    public static <T> int getCodeFromAttributeValue(ReadingTypeAttribute<?> definition, T attributeValue) {
        if (attributeValue == null) {
            return 0;
        }
        if (!attributeValue.getClass().isAssignableFrom(definition.getType())) {
            throw new IllegalArgumentException("Values must be " + definition.getType());
        }
        return ((ReadingTypeAttribute<T>) definition).getValueToCodeConverter().apply(attributeValue);
    }

    public static <T> T getAttributeValueFromCode(ReadingTypeAttribute<T> definition, int attributeValueCode) {
        return definition.getCodeToValueConverter().apply(attributeValueCode);
    }

    @ProviderType
    public abstract static class ReadingTypeAttribute<T> {
        private final Class<T> clazz;

        public ReadingTypeAttribute(Class<T> clazz) {
            this.clazz = clazz;
        }

        public Class<T> getType() {
            return this.clazz;
        }

        public Set<T> getPossibleValues() {
            return Collections.emptySet();
        }

        public boolean canBeWildcard() {
            return false;
        }

        public abstract Function<Integer, T> getCodeToValueConverter();

        public abstract Function<T, Integer> getValueToCodeConverter();

        public abstract Function<T, TranslationKey> getTranslationProvider();

        public abstract Function<ReadingType, T> getReadingTypeAttributeValue();
    }

    private abstract static class IntegerReadingTypeAttribute extends ReadingTypeAttribute<Integer> {
        IntegerReadingTypeAttribute() {
            super(Integer.class);
        }

        @Override
        public Function<Integer, Integer> getCodeToValueConverter() {
            return Function.identity();
        }

        @Override
        public Function<Integer, Integer> getValueToCodeConverter() {
            return Function.identity();
        }

        @Override
        public Function<Integer, TranslationKey> getTranslationProvider() {
            return value -> new SimpleTranslationKey(getType().getName() + "." + String.valueOf(value), String.valueOf(value));
        }
    }

}