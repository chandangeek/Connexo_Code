package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igh on 29/02/2016.
 */
public class ExpressionNodeParser {

    private Thesaurus thesaurus;

    public ExpressionNodeParser(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    private ArrayDeque<String> stack = new ArrayDeque<>();

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
        if (last.equals("constant")) {
            handleConstantNode(value);
        } else if ((last.equals("sum")) || (last.equals("avg")) || (last.equals("max")) || (last.equals("min"))) {
            handleFunctionNode(last);
        } else if ((last.equals("plus")) || (last.equals("minus")) || (last.equals("multiply")) || (last.equals("divide"))) {
            handleOperationNode(last);
        }
        removeArgumentCounter();
        incrementArgumentCounter();

    }

    private void handleConstantNode(String value) {
        nodes.add(new ConstantNode(new BigDecimal(value)));
    }

    private void handleOperationNode(String operator) {
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Operator '" + operator + "' requires at least 2 arguments");
        }
        OperationNode operationNode = new OperationNode(getOperator(operator), nodes.get(nodes.size() - 2), nodes.get(nodes.size() - 1), thesaurus);
        nodes.remove(nodes.size() - 2);
        nodes.remove(nodes.size() - 1);
        nodes.add(operationNode);
    }

    private void handleFunctionNode(String function) {
        if (nodes.size() < 1) {
            throw new IllegalArgumentException("Operator '" + function + "' requires at least 1 argument");
        }
        int numberOfArguments = getNumberOfArguments();
        FunctionCallNode functionCallNode = new FunctionCallNode(
                nodes.subList(nodes.size() - numberOfArguments, nodes.size()),
                getFunction(function), thesaurus);
        for (int i = 0; i < numberOfArguments; i++) {
            nodes.remove(nodes.size() - 1);
        }
        nodes.add(functionCallNode);
    }

    private Operator getOperator(String operator) {
        if ("plus".equals(operator)) {
            return Operator.PLUS;
        } else if ("minus".equals(operator)) {
            return Operator.MINUS;
        } else if ("multiply".equals(operator)) {
            return Operator.MULTIPLY;
        } else {
            return Operator.DIVIDE;
        }
    }

    private Function getFunction(String function) {
        if ("sum".equals(function)) {
            return Function.SUM;
        } else if ("avg".equals(function)) {
            return Function.AVG;
        } else if ("max".equals(function)) {
            return Function.MAX;
        } else {
            return Function.MIN;
        }
    }

    private void newArgumentCounter() {
        argumentCounters.add(Counters.newStrictCounter());
    }

    private void removeArgumentCounter() {
        argumentCounters.remove(argumentCounters.size() - 1);
    }

    private void incrementArgumentCounter() {
        if (argumentCounters.size() > 0) {
            argumentCounters.get(argumentCounters.size() - 1).increment();
        }
    }


    private int getNumberOfArguments() {
        return argumentCounters.get(argumentCounters.size() - 1).getValue();
    }


}
