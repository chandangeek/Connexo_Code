package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Created by igh on 29/02/2016.
 */
public class ExpressionNodeParser {

    private Thesaurus thesaurus;
    private MetrologyConfigurationService metrologyConfigurationService;
    private MetrologyConfiguration metrologyConfiguration;
    private Formula.Mode mode;

    public ExpressionNodeParser(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, MetrologyConfiguration metrologyConfiguration) {
        this(thesaurus, metrologyConfigurationService, metrologyConfiguration, Formula.Mode.AUTO);
    }

    public ExpressionNodeParser(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, MetrologyConfiguration metrologyConfiguration, Formula.Mode mode) {
        this.thesaurus = thesaurus;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.metrologyConfiguration = metrologyConfiguration;
        this.mode = mode;
    }

    private Deque<String> stack = new ArrayDeque<>();

    private List<ExpressionNode> nodes = new ArrayList<>();

    private List<Counter> argumentCounters = new ArrayList<> ();

    public ExpressionNode parse(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char value = input.charAt(i);
            if (value == '(') {
                stack.push(builder.toString());
                newArgumentCounter();
                builder = new StringBuilder();
            } else if (value == ')') {
                constructNode(builder.toString());
                builder = new StringBuilder();
            } else if (value == ',') {

            } else if (value == ' ') {

            }
            else {
                builder.append(value);
            }
        }
        return nodes.get(0);
    }

    private void constructNode(String value) {
        String last = stack.pop();
        if ("constant".equals(last)) {
            handleConstantNode(value);
        } else if (last.equals("D")) {
            handleDeliverableNode(value);
        } else if (last.equals("R")) {
            handleRequirementNode(value);
        } else if (Function.names().contains(last)) {
            handleFunctionNode(last);
        } else if (Operator.names().contains(last)) {
            handleOperationNode(last);
        }
        removeArgumentCounter();
        incrementArgumentCounter();

    }

    private void handleDeliverableNode(String value) {
        long id;
        try {
            id =  Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(value + " is not number");
        }
        Optional<ReadingTypeDeliverable> readingTypeDeliverable = metrologyConfigurationService.findReadingTypeDeliverable(id);
        if (readingTypeDeliverable.isPresent()) {
            if (!readingTypeDeliverable.get().getMetrologyConfiguration().equals(metrologyConfiguration)) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_METROLOGYCONFIGURATION_FOR_DELIVERABLE, (int) readingTypeDeliverable.get().getId());
            }
            if ((isAutoMode() && readingTypeDeliverable.get().getFormula().getMode().equals(Formula.Mode.EXPERT)) ||
                    (isExpertMode() && readingTypeDeliverable.get().getFormula().getMode().equals(Formula.Mode.AUTO))){
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
            if ((mode.equals(Formula.Mode.AUTO)) && (!readingTypeRequirement.get().isRegular())) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.IRREGULAR_READINGTYPE_IN_REQUIREMENT);
            }
            if ((mode.equals(Formula.Mode.AUTO) && (!UnitConversionSupport.isValidForAggregation(readingTypeRequirement.get().getUnit())))) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_READINGTYPE_IN_REQUIREMENT);
            }

            nodes.add(new ReadingTypeRequirementNodeImpl(readingTypeRequirement.get()));
        } else {
            throw new IllegalArgumentException("No requirement found with id " + id);
        }
    }

    private void handleConstantNode(String value) {
        nodes.add(new ConstantNodeImpl(new BigDecimal(value)));
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

    private void handleFunctionNode(String function) {
        if (mode.equals(Formula.Mode.AUTO)) {
            throw new InvalidNodeException(thesaurus, MessageSeeds.NO_FUNCTIONS_ALLOWED_IN_AUTOMODE);
        }
        if (nodes.size() < 1) {
            throw new IllegalArgumentException("Operator '" + function + "' requires at least 1 argument");
        }
        int numberOfArguments = getNumberOfArguments();
        FunctionCallNodeImpl functionCallNode = new FunctionCallNodeImpl(
                nodes.subList(nodes.size() - numberOfArguments, nodes.size()),
                getFunction(function), thesaurus);
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