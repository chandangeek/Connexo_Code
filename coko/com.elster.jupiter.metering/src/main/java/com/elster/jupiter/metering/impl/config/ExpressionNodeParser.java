package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
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

    public ExpressionNodeParser(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService) {
        this.thesaurus = thesaurus;
        this.metrologyConfigurationService = metrologyConfigurationService;
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
        } /*else if (last.equals("D")) {
            handleDeliverableNode(value);
        } else if (last.equals("R")) {
            handleRequirementNode(value);
        } */else if (Function.names().contains(last)) {
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
            nodes.add(new ReadingTypeRequirementNodeImpl(readingTypeRequirement.get()));
        } else {
            throw new IllegalArgumentException("No requirement found with id " + id);
        }
    }

    private void handleConstantNode(String value) {
        nodes.add(new ConstantNodeImpl(new BigDecimal(value)));
    }

    private void handleOperationNode(String operator) {
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Operator '" + operator + "' requires at least 2 arguments");
        }
        OperationNodeImpl operationNode = new OperationNodeImpl(getOperator(operator), nodes.get(nodes.size() - 2), nodes.get(nodes.size() - 1), thesaurus);
        nodes.remove(nodes.size() - 2);
        nodes.remove(nodes.size() - 1);
        nodes.add(operationNode);
    }

    private void handleFunctionNode(String function) {
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

}