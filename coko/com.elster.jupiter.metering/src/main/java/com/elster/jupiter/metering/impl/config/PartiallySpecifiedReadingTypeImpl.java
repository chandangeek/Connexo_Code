package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingType;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Dimension;

import javax.inject.Inject;
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

public class PartiallySpecifiedReadingTypeImpl extends ReadingTypeRequirementImpl implements PartiallySpecifiedReadingType {
    public static final String TYPE_IDENTIFIER = "PRT";

    private final DataModel dataModel;

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeTemplate> readingTypeTemplate = ValueReference.absent();
    private List<PartiallySpecifiedReadingTypeAttributeValueImpl> overriddenAttributes = new ArrayList<>(ReadingTypeTemplateAttributeName.values().length);

    private Collection<Function<ReadingType, Boolean>> attributeMatchers;
    private Dimension dimension;

    @Inject
    public PartiallySpecifiedReadingTypeImpl(DataModel dataModel, ServerMetrologyConfigurationService metrologyConfigurationService) {
        super(metrologyConfigurationService);
        this.dataModel = dataModel;
    }

    public PartiallySpecifiedReadingTypeImpl init(MetrologyConfiguration metrologyConfiguration, String name, ReadingTypeTemplate template) {
        super.init(metrologyConfiguration, name);
        this.readingTypeTemplate.set(template);
        return this;
    }

    @Override
    public ReadingTypeTemplate getReadingTypeTemplate() {
        return this.readingTypeTemplate.get();
    }

    @Override
    public String getDescription() {
        List<Optional<String>> valueElements = new ArrayList<>();
        valueElements.add(Stream.of(getAttributeValue(ReadingTypeTemplateAttributeName.MACRO_PERIOD), getAttributeValue(ReadingTypeTemplateAttributeName.ACCUMULATION))
                .flatMap(com.elster.jupiter.util.streams.Functions.asStream()).findFirst().map(v -> "[" + v + "]"));
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
        if (possibleValues.size() == 1) {
            return translate(attributeName.getDefinition(), possibleValues.get(0));
        } else {
            return attributeName.getDefinition().canBeWildcard() ? Optional.of("*") : Optional.empty();
        }
    }

    private Optional<String> getTemplateValue(ReadingTypeTemplateAttributeName attributeName, String prefix) {
        List<Integer> possibleValues = this.getReadingTypeTemplate().getAttribute(attributeName).getPossibleValues();
        if (possibleValues.size() == 1) {
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

    public boolean isRegular() {
        boolean wildCardForMacroPeriod = false;
        boolean wildCardForTime = false;
        for (PartiallySpecifiedReadingTypeAttributeValueImpl att : overriddenAttributes) {
            if ((att.getName() == ReadingTypeTemplateAttributeName.MACRO_PERIOD) && (!wildCardForMacroPeriod)) {
                wildCardForMacroPeriod = true;
            }
            if ((att.getName() == ReadingTypeTemplateAttributeName.TIME) && (!wildCardForTime)) {
                wildCardForTime = true;
            }
        }
        if (!wildCardForMacroPeriod) {
            ReadingTypeTemplateAttribute macroPeriodAttribute = getReadingTypeTemplate().getAttribute(ReadingTypeTemplateAttributeName.MACRO_PERIOD);
            if (macroPeriodAttribute.getCode().isPresent()) {
                int value = macroPeriodAttribute.getCode().get();
                if ((value != MacroPeriod.DAILY.ordinal()) && (value != MacroPeriod.MONTHLY.ordinal())) {
                    return false;
                }
            }
        }
        if (!wildCardForTime) {
            ReadingTypeTemplateAttribute timeAttribute = getReadingTypeTemplate().getAttribute(ReadingTypeTemplateAttributeName.TIME);
            if (timeAttribute.getCode().isPresent()) {
                int value = timeAttribute.getCode().get();
                if (value != TimeAttribute.NOTAPPLICABLE.ordinal()) {
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
        // the unit attribute always have a code or possible values, but let's check it
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
            this.overriddenAttributes.stream().forEach(attr -> attributeMatchersMap.put(attr.getName(),
                    rt -> ReadingTypeTemplateAttributeName.getReadingTypeAttributeCode(attr.getName().getDefinition(), rt) == attr.getCode()));
            this.attributeMatchers = attributeMatchersMap.values();
        }
        return this.attributeMatchers.stream().allMatch(matcher -> matcher.apply(readingType));
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

    void touch() {
        this.dataModel.touch(this);
    }

    @Override
    public PartiallySpecifiedReadingType overrideAttribute(ReadingTypeTemplateAttributeName name, int code) {
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
    public PartiallySpecifiedReadingType removeOverriddenAttribute(ReadingTypeTemplateAttributeName name) {
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
}
