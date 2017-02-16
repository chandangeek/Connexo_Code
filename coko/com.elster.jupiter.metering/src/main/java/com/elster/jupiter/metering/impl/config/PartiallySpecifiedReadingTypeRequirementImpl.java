/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.units.Dimension;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PartiallySpecifiedReadingTypeRequirementImpl extends ReadingTypeRequirementImpl implements PartiallySpecifiedReadingTypeRequirement {
    public static final String TYPE_IDENTIFIER = "PRT";

    private final DataModel dataModel;

    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Long readingTypeTemplateId;
    private transient ReadingTypeTemplate cachedReadingTypeTemplate;
    private List<PartiallySpecifiedReadingTypeAttributeValueImpl> overriddenAttributes = new ArrayList<>(ReadingTypeTemplateAttributeName.values().length);

    private Collection<Function<ReadingType, Boolean>> attributeMatchers;
    private Dimension dimension;

    @Inject
    PartiallySpecifiedReadingTypeRequirementImpl(DataModel dataModel, ServerMetrologyConfigurationService metrologyConfigurationService) {
        super(metrologyConfigurationService);
        this.dataModel = dataModel;
    }

    public PartiallySpecifiedReadingTypeRequirementImpl init(MetrologyConfiguration metrologyConfiguration, String name, ReadingTypeTemplate template) {
        super.init(metrologyConfiguration, name);
        this.readingTypeTemplateId = template != null ? template.getId() : null;
        return this;
    }

    @Override
    public ReadingTypeTemplate getReadingTypeTemplate() {
        if (cachedReadingTypeTemplate == null) {
            cachedReadingTypeTemplate = this.getMetrologyConfigurationService().findReadingTypeTemplate(readingTypeTemplateId)
                    .orElseThrow(() -> new IllegalStateException("No such reading type template with id = " + readingTypeTemplateId));
        }
        return cachedReadingTypeTemplate;
    }

    @Override
    public String getDescription() {
        List<Optional<String>> valueElements = new ArrayList<>();
        if(!getAttributeValue(ReadingTypeTemplateAttributeName.MACRO_PERIOD).equals(Optional.of(MacroPeriod.NOTAPPLICABLE.getDescription()))){
            valueElements.add(Stream.of(getAttributeValue(ReadingTypeTemplateAttributeName.MACRO_PERIOD), getAttributeValue(ReadingTypeTemplateAttributeName.ACCUMULATION))
                    .flatMap(com.elster.jupiter.util.streams.Functions.asStream()).findFirst().map(v -> "[" + v + "]"));
        }
        valueElements.add(getAttributeValue(ReadingTypeTemplateAttributeName.ACCUMULATION));
        valueElements.add(getAttributeValue(ReadingTypeTemplateAttributeName.FLOW_DIRECTION));
        valueElements.add(getAttributeValue(ReadingTypeTemplateAttributeName.COMMODITY));
        valueElements.add(Optional.ofNullable(this.getReadingTypeTemplate().getName()));
        valueElements.add(getUnitWithMultiplierValue());
        valueElements.add(getAttributeValue(ReadingTypeTemplateAttributeName.PHASE));
        valueElements.add(getAttributeValue(ReadingTypeTemplateAttributeName.TIME_OF_USE, "ToU"));
        valueElements.add(getAttributeValue(ReadingTypeTemplateAttributeName.CRITICAL_PEAK_PERIOD, "CPP"));
        valueElements.add(getAttributeValue(ReadingTypeTemplateAttributeName.CONSUMPTION_TIER, "Tier"));
        return valueElements.stream()
                .flatMap(com.elster.jupiter.util.streams.Functions.asStream())
                .collect(Collectors.joining(" "));
    }

    @Override
    public Optional<String> getAttributeValue(ReadingTypeTemplateAttributeName attributeName) {
        return overriddenAttributes.stream()
                .filter(a -> a.getName().equals(attributeName))
                .findFirst()
                .map(PartiallySpecifiedReadingTypeAttributeValueImpl::getCode)
                .map(e -> translate(attributeName.getDefinition(), e))
                .orElseGet(() -> getTemplateValue(attributeName));
    }

    private Optional<String> getAttributeValue(ReadingTypeTemplateAttributeName attributeName, String prefix) {
        return overriddenAttributes.stream()
                .filter(a -> a.getName().equals(attributeName))
                .findFirst()
                .map(PartiallySpecifiedReadingTypeAttributeValueImpl::getCode)
                .map(e -> Optional.of(prefix + e))
                .orElseGet(() -> getTemplateValue(attributeName, prefix));
    }

    @Override
    public List<Optional<String>> getAttributeValues(ReadingTypeTemplateAttributeName attributeName) {
        return overriddenAttributes.stream()
                .filter(a -> a.getName().equals(attributeName))
                .findFirst().map(PartiallySpecifiedReadingTypeAttributeValueImpl::getCode).map(o ->
                        Collections.singletonList(translate(attributeName.getDefinition(), o)))
                .orElseGet(() ->
                        this.getReadingTypeTemplate()
                                .getAttribute(attributeName)
                                .getPossibleValues()
                                .stream()
                                .map(e -> translate(attributeName.getDefinition(), e))
                                .collect(Collectors.toList())
                );
    }

    private Optional<String> getUnitWithMultiplierValue() {
        String multiplier = getAttributeValue(ReadingTypeTemplateAttributeName.METRIC_MULTIPLIER).orElse("");
        List<String> units = getAttributeValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE).stream()
                .flatMap(com.elster.jupiter.util.streams.Functions.asStream())
                .map(e -> multiplier + e).collect(Collectors.toList());
        if (!units.isEmpty()) {
            return Optional.of("(" + String.join(", ", units) + ")");
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> getTemplateValue(ReadingTypeTemplateAttributeName attributeName) {
        List<Integer> possibleValues = this.getReadingTypeTemplate().getAttribute(attributeName).getPossibleValues();
        if (possibleValues.size() == 1
                && Stream.of(ReadingTypeTemplateAttributeName.ACCUMULATION,
                ReadingTypeTemplateAttributeName.FLOW_DIRECTION,
                ReadingTypeTemplateAttributeName.COMMODITY).noneMatch(a -> a.equals(attributeName))) {
            return translate(attributeName.getDefinition(), possibleValues.get(0));
        } else {
            return attributeName.getDefinition().canBeWildcard() ? Optional.of("*") : Optional.empty();
        }
    }

    private Optional<String> getTemplateValue(ReadingTypeTemplateAttributeName attributeName, String prefix) {
        List<Integer> possibleValues = this.getReadingTypeTemplate().getAttribute(attributeName).getPossibleValues();
        if (possibleValues.size() == 1
                && Stream.of(ReadingTypeTemplateAttributeName.ACCUMULATION,
                ReadingTypeTemplateAttributeName.FLOW_DIRECTION,
                ReadingTypeTemplateAttributeName.COMMODITY).noneMatch(a -> a.equals(attributeName))) {
            return Optional.of(prefix + possibleValues.get(0));
        } else {
            return attributeName.getDefinition().canBeWildcard() ? Optional.of("*") : Optional.empty();
        }
    }

    private <T> Optional<String> translate(ReadingTypeTemplateAttributeName.ReadingTypeAttribute<T> definition, Integer code) {
        return Optional.ofNullable(this.getMetrologyConfigurationService()
                .getThesaurus()
                .getFormat(definition.getTranslationProvider().apply(definition.getCodeToValueConverter().apply(code)))
                .format());
    }

    @Override
    public Dimension getDimension() {
        if (this.dimension == null) {
            this.dimension = this.overriddenAttributes
                    .stream()
                    .filter(attr -> ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE == attr.getName())
                    .map(PartiallySpecifiedReadingTypeAttributeValueImpl::getCode)
                    .map(this::getDimensionFromReadingTypeUnitCode)
                    .findAny()
                    .orElseGet(this::getDimensionFromTemplate);
        }
        return this.dimension;
    }

    @Override
    public boolean isRegular() {
        if (!hasWildCardForMacroPeriod()) {
            ReadingTypeTemplateAttribute macroPeriodAttribute = getReadingTypeTemplate().getAttribute(ReadingTypeTemplateAttributeName.MACRO_PERIOD);
            if (macroPeriodAttribute.getCode().isPresent()) {
                int value = macroPeriodAttribute.getCode().get();
                if ((value != MacroPeriod.DAILY.getId()) && (value != MacroPeriod.MONTHLY.getId())) {
                    return false;
                }
            }
        }
        if (!hasWildCardForTime()) {
            ReadingTypeTemplateAttribute timeAttribute = getReadingTypeTemplate().getAttribute(ReadingTypeTemplateAttributeName.TIME);
            if (timeAttribute.getCode().isPresent()) {
                int value = timeAttribute.getCode().get();
                if (value == TimeAttribute.NOTAPPLICABLE.getId()) {
                    return false;
                }
            }
        }
        return true;
    }

    private Dimension getDimensionFromTemplate() {
        ReadingTypeTemplateAttribute unitAttribute = getReadingTypeTemplate().getAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE);
        if (unitAttribute.getCode().isPresent()) {
            return getDimensionFromReadingTypeUnitCode(unitAttribute.getCode().get());
        }
        // the unit attribute always has a code or possible values, but let's check it
        if (unitAttribute.getPossibleValues().isEmpty()) {
            throw new IllegalStateException("The UNIT_OF_MEASURE has no code and no possible values in reading type template '"
                    + getReadingTypeTemplate().getName() + "'.");
        }
        // all possible values have the same dimension
        return getDimensionFromReadingTypeUnitCode(unitAttribute.getPossibleValues().get(0));
    }

    private Dimension getDimensionFromReadingTypeUnitCode(int code) {
        return ((ReadingTypeUnit) ReadingTypeTemplateAttributeName.getAttributeValueFromCode(
                ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE.getDefinition(), code)).getUnit().getDimension();
    }

    @Override
    public boolean matches(ReadingType readingType) {
        if (this.attributeMatchers == null) {
            Map<ReadingTypeTemplateAttributeName, Function<ReadingType, Boolean>> attributeMatchersMap = getReadingTypeTemplate().getAttributes()
                    .stream()
                    .collect(Collectors.toMap(ReadingTypeTemplateAttribute::getName, this::getMatcherWithSystemPossibleValues));
            this.overriddenAttributes.forEach(attr -> attributeMatchersMap.put(attr.getName(),
                    rt -> ReadingTypeTemplateAttributeName.getReadingTypeAttributeCode(attr.getName().getDefinition(), rt) == attr.getCode()));
            this.attributeMatchers = attributeMatchersMap.values();
        }
        return getReadingTypeTemplate().getReadingTypeRestrictions().stream().allMatch(e -> e.test(readingType))
                && this.attributeMatchers.stream().allMatch(matcher -> matcher.apply(readingType));
    }

    private Function<ReadingType, Boolean> getMatcherWithSystemPossibleValues(ReadingTypeTemplateAttribute attribute) {
        if (attribute.getCode().isPresent() || !attribute.getPossibleValues().isEmpty()) {
            return attribute::matches;
        }
        ReadingTypeTemplateAttributeName.ReadingTypeAttribute<?> definition = attribute.getName().getDefinition();
        Set<?> systemPossibleValues = definition.getPossibleValues()
                .stream()
                .map(possibleValue -> ReadingTypeTemplateAttributeName.getCodeFromAttributeValue(definition, possibleValue))
                .collect(Collectors.toSet());
        return !systemPossibleValues.isEmpty()
                ? rt -> systemPossibleValues.contains(ReadingTypeTemplateAttributeName.getReadingTypeAttributeCode(definition, rt))
                : rt -> true;
    }

    private void touch() {
        this.dataModel.touch(this);
    }

    @Override
    public PartiallySpecifiedReadingTypeRequirement overrideAttribute(ReadingTypeTemplateAttributeName name, int code) {
        PartiallySpecifiedReadingTypeAttributeValueImpl value = this.dataModel.getInstance(PartiallySpecifiedReadingTypeAttributeValueImpl.class);
        value.init(this, name, code);
        Save.CREATE.validate(this.dataModel, value);
        this.overriddenAttributes.remove(value);
        this.overriddenAttributes.add(value);
        touch();
        this.attributeMatchers = null;
        return this;
    }

    @Override
    public PartiallySpecifiedReadingTypeRequirement removeOverriddenAttribute(ReadingTypeTemplateAttributeName name) {
        if (!this.overriddenAttributes.isEmpty()) {
            ListIterator<PartiallySpecifiedReadingTypeAttributeValueImpl> itr = this.overriddenAttributes.listIterator();
            while (itr.hasNext()) {
                if (itr.next().getName() == name) {
                    itr.remove();
                    touch();
                    this.attributeMatchers = null;
                    return this;
                }
            }
        }
        return this;
    }

    @Override
    public MacroPeriod getMacroPeriod() {
        ReadingTypeTemplateAttribute macroPeriodAttribute = getReadingTypeTemplate().getAttribute(ReadingTypeTemplateAttributeName.MACRO_PERIOD);
        if (macroPeriodAttribute.getCode().isPresent()) {
            int macroPeriod = macroPeriodAttribute.getCode().get();
            if (macroPeriod == MacroPeriod.DAILY.getId()) {
                return MacroPeriod.DAILY;
            } else if (macroPeriod == MacroPeriod.MONTHLY.getId()) {
                return MacroPeriod.MONTHLY;
            } else {
                return MacroPeriod.NOTAPPLICABLE;
            }
        }
        return MacroPeriod.NOTAPPLICABLE;
    }

    @Override
    public TimeAttribute getMeasuringPeriod() {
        ReadingTypeTemplateAttribute timeAttribute = getReadingTypeTemplate().getAttribute(ReadingTypeTemplateAttributeName.TIME);
        if (timeAttribute.getCode().isPresent()) {
            return TimeAttribute.get(timeAttribute.getCode().get());
        }
        return TimeAttribute.NOTAPPLICABLE;
    }

    @Override
    public Set<ReadingTypeUnit> getUnits() {
        ReadingTypeTemplateAttribute unitAttribute = getReadingTypeTemplate().getAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE);
        Set<ReadingTypeUnit> readingTypeUnits = unitAttribute.getPossibleValues().stream().map(ReadingTypeUnit::get).collect(Collectors.toSet());
        if (unitAttribute.getCode().isPresent()) {
            readingTypeUnits.add(ReadingTypeUnit.get(unitAttribute.getCode().get()));
        }
        return readingTypeUnits;
    }

    @Override
    public IntervalLength getIntervalLength() {
        return IntervalLength.from(this);
    }

    private boolean hasWildCardForTime() {
        return hasWildcardFor(ReadingTypeTemplateAttributeName.TIME);
    }

    private boolean hasWildCardForMacroPeriod() {
        return hasWildcardFor(ReadingTypeTemplateAttributeName.MACRO_PERIOD);
    }

    private boolean hasWildcardFor(ReadingTypeTemplateAttributeName attribute) {
        ReadingTypeTemplateAttribute templateAttribute = this.getReadingTypeTemplate().getAttribute(attribute);
        // 1. attribute definition allows wildcards
        // 2. user doesn't override this attribute in PartiallySpecifiedReadingType (because if he does this attribute has value)
        // 3. there is no value for that attribute in template
        // 4. there is no possible values for that attribute in template
        return attribute.getDefinition().canBeWildcard() && !this.overriddenAttributes.stream()
                .map(PartiallySpecifiedReadingTypeAttributeValueImpl::getName)
                .anyMatch(attribute::equals)
                && !templateAttribute.getCode().isPresent() && templateAttribute.getPossibleValues().isEmpty();
    }

}
