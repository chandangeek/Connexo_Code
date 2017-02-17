/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.slp.SyntheticLoadProfile;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by igh on 29/02/2016.
 */
public class ExpressionNodeParser {

    private final Thesaurus thesaurus;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final CustomPropertySetService customPropertySetService;
    private final MetrologyConfiguration metrologyConfiguration;
    private final Formula.Mode mode;

    public ExpressionNodeParser(Thesaurus thesaurus, ServerMetrologyConfigurationService metrologyConfigurationService, CustomPropertySetService customPropertySetService, MetrologyConfiguration metrologyConfiguration, Formula.Mode mode) {
        this.thesaurus = thesaurus;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.customPropertySetService = customPropertySetService;
        this.metrologyConfiguration = metrologyConfiguration;
        this.mode = mode;
    }

    private Deque<String> tokens = new ArrayDeque<>();
    private Deque<String> customPropertySetIds = new ArrayDeque<>();
    private Deque<AggregationLevel> aggregationLevels = new ArrayDeque<>();

    private List<ServerExpressionNode> nodes = new ArrayList<>();

    private List<Counter> argumentCounters = new ArrayList<>();

    public ServerExpressionNode parse(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char value = input.charAt(i);
            if (value == '(') {
                tokens.push(builder.toString());
                newArgumentCounter();
                builder = new StringBuilder();
            } else if (value == ')') {
                constructNode(builder.toString());
                builder = new StringBuilder();
            } else if (value == ',') {
                if ("property".equals(this.tokens.peek())) {
                    this.customPropertySetIds.push(builder.toString());
                    builder = new StringBuilder();
                } else {
                    Optional<AggregationLevel> aggregationLevel = AggregationLevel.from(builder.toString());
                    if (aggregationLevel.isPresent()) {
                        this.aggregationLevels.push(aggregationLevel.get());
                        builder = new StringBuilder();
                    }
                }
            } else if (value == ' ') {
                // Ignore whitespace
            } else {
                builder.append(value);
            }
        }
        return nodes.get(0);
    }

    private void constructNode(String currentToken) {
        String lastToken = tokens.pop();
        if ("constant".equals(lastToken)) {
            handleConstantNode(currentToken);
        } else if ("property".equals(lastToken)) {
            handlePropertyNode(currentToken);
        } else if ("D".equals(lastToken)) {
            handleDeliverableNode(currentToken);
        } else if ("R".equals(lastToken)) {
            handleRequirementNode(currentToken);
        } else if (Function.names().contains(lastToken)) {
            AggregationLevel.from(currentToken).ifPresent(this.aggregationLevels::push);
            handleFunctionNode(lastToken);
        } else if (Operator.names().contains(lastToken)) {
            if ("null".equals(currentToken)) {
                handleNullNode();
            }
            handleOperationNode(lastToken);
        } else {
            throw new IllegalArgumentException("Unexpected token: " + currentToken);
        }
        removeArgumentCounter();
        incrementArgumentCounter();
    }

    private void handleDeliverableNode(String value) {
        long id;
        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(value + " is not number");
        }
        Optional<ReadingTypeDeliverable> readingTypeDeliverable = metrologyConfigurationService.findReadingTypeDeliverable(id);
        if (readingTypeDeliverable.isPresent()) {
            if (!readingTypeDeliverable.get().getMetrologyConfiguration().equals(metrologyConfiguration)) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_METROLOGYCONFIGURATION_FOR_DELIVERABLE, (int) readingTypeDeliverable.get().getId());
            }
            if ((isAutoMode() && readingTypeDeliverable.get().getFormula().getMode().equals(Formula.Mode.EXPERT)) ||
                    (isExpertMode() && readingTypeDeliverable.get().getFormula().getMode().equals(Formula.Mode.AUTO))) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.AUTO_AND_EXPERT_MODE_CANNOT_BE_COMBINED);
            }
            nodes.add(new ReadingTypeDeliverableNodeImpl(readingTypeDeliverable.get()));
        } else {
            throw new IllegalArgumentException("No deliverable found with id " + id);
        }
    }

    private void handleRequirementNode(String value) {
        long id;
        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(value + " is not number");
        }
        Optional<ReadingTypeRequirement> readingTypeRequirement = metrologyConfigurationService.findReadingTypeRequirement(id);
        if (readingTypeRequirement.isPresent()) {
            if (!readingTypeRequirement.get().getMetrologyConfiguration().equals(metrologyConfiguration)) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_METROLOGYCONFIGURATION_FOR_REQUIREMENT, (int) readingTypeRequirement.get().getId());
            }
            if ((mode.equals(Formula.Mode.AUTO) && (!UnitConversionSupport.isValidForAggregation(readingTypeRequirement.get().getUnits())))) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_READINGTYPE_UNIT_IN_REQUIREMENT);
            }

            nodes.add(new ReadingTypeRequirementNodeImpl(readingTypeRequirement.get()));
        } else {
            throw new IllegalArgumentException("No requirement found with id " + id);
        }
    }

    private void handleConstantNode(String value) {
        nodes.add(new ConstantNodeImpl(new BigDecimal(value)));
    }

    @SuppressWarnings("unchecked")
    private void handlePropertyNode(String propertyName) {
        String customPropertySetId = this.customPropertySetIds.pop();
        Optional<RegisteredCustomPropertySet> activeCustomPropertySet = this.customPropertySetService.findActiveCustomPropertySet(customPropertySetId);
        if (activeCustomPropertySet.isPresent()) {
            CustomPropertySet customPropertySet = activeCustomPropertySet.get().getCustomPropertySet();
            List<PropertySpec> propertySpecs = activeCustomPropertySet.get().getCustomPropertySet().getPropertySpecs();
            Optional<PropertySpec> propertySpec = propertySpecs.stream().filter(each -> propertyName.equals(each.getName())).findFirst();
            if (!propertySpec.isPresent()) {
                throw new IllegalArgumentException("No property with name " + propertyName + " found in custom property set found with id " + customPropertySetId);
            }
            if (!this.customPropertySetIsConfiguredOnMetrologyConfiguration(customPropertySet)) {
                throw InvalidNodeException.customPropertyNotConfigured(this.thesaurus, propertySpec.get(), customPropertySet);
            }
            if (!activeCustomPropertySet.get().getCustomPropertySet().isVersioned()) {
                throw InvalidNodeException.customPropertySetNotVersioned(this.thesaurus, customPropertySet);
            }
            this.checkCompatibility(propertySpec.get(), customPropertySet);
            this.nodes.add(new CustomPropertyNodeImpl(propertySpec.get(), activeCustomPropertySet.get()));
        } else {
            throw new IllegalArgumentException("No custom property set found with id " + customPropertySetId);
        }
    }

    private boolean customPropertySetIsConfiguredOnMetrologyConfiguration(CustomPropertySet customPropertySet) {
        return this.metrologyConfiguration
                .getCustomPropertySets()
                .stream()
                .anyMatch(each -> each.getCustomPropertySet().getId().equals(customPropertySet.getId()));
    }

    private void checkCompatibility(PropertySpec propertySpec, CustomPropertySet customPropertySet) {
        if (propertySpec.isReference()) {
            if (!this.isCompatible(propertySpec, SyntheticLoadProfile.class)) {
                throw InvalidNodeException.customPropertyMustBeSyntheticLoadProfile(this.thesaurus, customPropertySet, propertySpec);
            }
        } else if (!this.isCompatible(propertySpec, Number.class, Quantity.class)) {
            throw InvalidNodeException.customPropertyMustBeNumerical(this.thesaurus, customPropertySet, propertySpec);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isCompatible(PropertySpec propertySpec, Class...supportedValueTypes) {
        Class valueType = propertySpec.getValueFactory().getValueType();
        return Stream.of(supportedValueTypes).anyMatch(each -> each.isAssignableFrom(valueType));
    }

    private void handleNullNode() {
        nodes.add(new NullNodeImpl());
    }

    private void handleOperationNode(String operatorValue) {
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Operator '" + operatorValue + "' requires at least 2 arguments");
        }
        Operator operator = getOperator(operatorValue);
        OperationNodeImpl operationNode;
        if (operator.equals(Operator.SAFE_DIVIDE)) {
            operationNode = new OperationNodeImpl(operator, nodes.get(nodes.size() - 3), nodes.get(nodes.size() - 2), nodes.get(nodes.size() - 1), thesaurus);
            nodes.remove(nodes.size() - 3);
            nodes.remove(nodes.size() - 2);
            nodes.remove(nodes.size() - 1);
        } else {
            operationNode = new OperationNodeImpl(operator, nodes.get(nodes.size() - 2), nodes.get(nodes.size() - 1), thesaurus);
            nodes.remove(nodes.size() - 2);
            nodes.remove(nodes.size() - 1);
        }
        nodes.add(operationNode);
    }

    private void handleFunctionNode(String functionName) {
        if (nodes.size() < 1) {
            throw new IllegalArgumentException("Operator '" + functionName + "' requires at least 1 argument");
        }
        int numberOfArguments = getNumberOfArguments();
        Function function = getFunction(functionName);
        if (!this.mode.supports(function)) {
            throw InvalidNodeException.functionNotAllowedInAutoMode(thesaurus, function);
        }
        AggregationLevel aggregationLevel = null;
        if (function.requiresAggregationLevel()) {
            if (this.aggregationLevels.isEmpty()) {
                throw new IllegalArgumentException("Aggregation function '" + functionName + "' requires aggregation level");
            }
            aggregationLevel = this.aggregationLevels.pop();
        }
        FunctionCallNodeImpl functionCallNode =
                new FunctionCallNodeImpl(
                        nodes.subList(nodes.size() - numberOfArguments, nodes.size()),
                        function,
                        aggregationLevel,
                        thesaurus);
        for (int i = 0; i < numberOfArguments; i++) {
            nodes.remove(nodes.size() - 1);
        }
        nodes.add(functionCallNode);
    }

    private Operator getOperator(String operator) {
        return Operator.from(operator).get();
    }

    private Function getFunction(String function) {
        return Function.from(function).get();
    }

    private void newArgumentCounter() {
        argumentCounters.add(Counters.newStrictCounter());
    }

    private void removeArgumentCounter() {
        argumentCounters.remove(argumentCounters.size() - 1);
    }

    private void incrementArgumentCounter() {
        if (!argumentCounters.isEmpty()) {
            argumentCounters.get(argumentCounters.size() - 1).increment();
        }
    }

    private int getNumberOfArguments() {
        return argumentCounters.get(argumentCounters.size() - 1).getValue();
    }

    private boolean isAutoMode() {
        return mode.equals(Formula.Mode.AUTO);
    }

    private boolean isExpertMode() {
        return !isAutoMode();
    }

}