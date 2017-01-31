/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains an entry for every {@link com.elster.jupiter.metering.config.Function}
 * to provide a {@link TranslationKey} for that Function.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-12 (14:39)
 */
enum FunctionTranslationKey implements TranslationKey {

    SUM(Function.SUM, "sum({0}, {1})"),
    MAX(Function.MAX, "max") {
        @Override
        protected String format(Thesaurus thesaurus, AggregationLevel aggregationLevel, List<ExpressionNode> arguments, FormulaDescriptionBuilder builder) {
            return this.formatVarArgs(arguments, builder);
        }
    },
    MIN(Function.MIN, "min") {
        @Override
        protected String format(Thesaurus thesaurus, AggregationLevel aggregationLevel, List<ExpressionNode> arguments, FormulaDescriptionBuilder builder) {
            return this.formatVarArgs(arguments, builder);
        }
    },
    MAX_AGG(Function.MAX_AGG, "max({0}, {1})"),
    MIN_AGG(Function.MIN_AGG, "min({0}, {1})"),
    AVG(Function.AVG, "avg({0}, {1})"),
    AGG_TIME(Function.AGG_TIME, "aggregate({0})"),
    POWER(Function.POWER, "power({0}, {1})"),
    SQRT(Function.SQRT, "square root({0}, {1})"),
    FIRST_NOT_NULL(Function.FIRST_NOT_NULL, "firstNotNull") {
        @Override
        protected String format(Thesaurus thesaurus, AggregationLevel aggregationLevel, List<ExpressionNode> arguments, FormulaDescriptionBuilder builder) {
            return this.formatVarArgs(arguments, builder);
        }
    };

    private final Function function;
    private final String defaultFormat;

    FunctionTranslationKey(Function function, String defaultFormat) {
        this.function = function;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return Function.class.getName() + this.function.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static FunctionTranslationKey from(Function function) {
        return Stream.of(values())
                    .filter(each -> each.function.equals(function))
                    .findFirst()
                    .orElseThrow( () -> new IllegalArgumentException("Unknown function " + function.name()));
    }

    public static String format(Thesaurus thesaurus, FunctionCallNode functionCall, FormulaDescriptionBuilder builder) {
        return from(functionCall.getFunction())
                .format(
                    thesaurus,
                    functionCall.getAggregationLevel().orElse(null),
                    functionCall.getChildren(),
                    builder);
    }

    protected String format(Thesaurus thesaurus, AggregationLevel aggregationLevel, List<ExpressionNode> arguments, FormulaDescriptionBuilder builder) {
        return thesaurus
                .getFormat(this)
                .format(describeAll(arguments, builder).toArray());
    }

    protected String formatVarArgs(List<ExpressionNode> arguments, FormulaDescriptionBuilder builder) {
        return this.defaultFormat + "(" + describeAll(arguments, builder).collect(Collectors.joining(", ")) + ")";
    }

    private Stream<String> describeAll(List<ExpressionNode> arguments, FormulaDescriptionBuilder builder) {
        return arguments
                    .stream()
                    .map(node -> node.accept(builder));
    }

}