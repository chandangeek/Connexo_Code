/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

/**
 * Contains an entry for every {@link Operator}
 * to provide a {@link TranslationKey} for that Function.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-12 (15:36)
 */
enum OperatorTranslationKey implements TranslationKey {

    PLUS(Operator.PLUS, "{0} + {1}"),
    MINUS(Operator.MINUS, "{0} - {1}"),
    MULTIPLY(Operator.MULTIPLY, "{0} * {1}"),
    DIVIDE(Operator.DIVIDE, "{0} / {1}"),
    SAFE_DIVIDE(Operator.SAFE_DIVIDE, "{0} / ({1} or {2})") {
        @Override
        protected Stream<ExpressionNode> getArgumentsStream(OperationNode node) {
            return Stream.of(node.getLeftOperand(), node.getRightOperand(), node.getZeroReplacement().get());
        }
    };

    private final Operator operator;
    private final String defaultFormat;

    OperatorTranslationKey(Operator operator, String defaultFormat) {
        this.operator = operator;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return Operator.class.getName() + this.operator.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static OperatorTranslationKey from(Operator operator) {
        return Stream.of(values())
                    .filter(each -> each.operator.equals(operator))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown operator " + operator.name()));
    }

    public static String format(Thesaurus thesaurus, OperationNode node, FormulaDescriptionBuilder builder) {
        OperatorTranslationKey value = from(node.getOperator());
        return value.format(thesaurus, value.getArgumentsStream(node), builder);
    }

    protected Stream<ExpressionNode> getArgumentsStream(OperationNode node) {
        return Stream.of(node.getLeftOperand(), node.getRightOperand());
    }

    private String format(Thesaurus thesaurus, Stream<ExpressionNode> arguments, FormulaDescriptionBuilder builder) {
        return thesaurus
                .getFormat(this)
                .format(describeAll(arguments, builder).toArray());
    }

    private Stream<String> describeAll(Stream<ExpressionNode> arguments, FormulaDescriptionBuilder builder) {
        return arguments.map(node -> node.accept(builder));
    }

}