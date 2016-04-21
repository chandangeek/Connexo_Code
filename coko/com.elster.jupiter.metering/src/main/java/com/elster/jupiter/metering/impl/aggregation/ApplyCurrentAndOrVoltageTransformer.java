package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * that applies current and/or voltage transformer configuration according to
 * the rules defined by: <a href="https://confluence.eict.vpdc/display/COPL/Support+for+CT%2C+VT+and+transformer+on+meter+and+usagepoint">confluence</a>.
 * <p>
 * It is very important to apply this transformation only after the preferred interval has been determined
 * because this component needs the reading type of the preferred channel to determine which Multiplier is needed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:04)
 */
class ApplyCurrentAndOrVoltageTransformer implements ServerExpressionNode.Visitor<ServerExpressionNode> {

    private final MeteringService meteringService;
    private final MeterActivation meterActivation;

    ApplyCurrentAndOrVoltageTransformer(MeteringService meteringService, MeterActivation meterActivation) {
        this.meteringService = meteringService;
        this.meterActivation = meterActivation;
    }

    @Override
    public ServerExpressionNode visitVirtualRequirement(VirtualRequirementNode requirement) {
        ReadingType readingType = requirement.getPreferredChannel().getMainReadingType();
        return RequirementNodeReplacer
                .from(readingType)
                .apply(new Context(
                        requirement,
                        this.meteringService,
                        this.meterActivation));
    }

    @Override
    public ServerExpressionNode visitConstant(NumericalConstantNode constant) {
        // No replacement
        return constant;
    }

    @Override
    public ServerExpressionNode visitConstant(StringConstantNode constant) {
        // No replacement
        return constant;
    }

    @Override
    public ServerExpressionNode visitNull(NullNodeImpl nullNode) {
        // No replacement
        return nullNode;
    }

    @Override
    public ServerExpressionNode visitVariable(VariableReferenceNode variable) {
        // No replacement
        return variable;
    }

    @Override
    public ServerExpressionNode visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        // No replacement
        return deliverable;
    }

    @Override
    public ServerExpressionNode visitOperation(OperationNode operationNode) {
        // Copy as child nodes may be replaced
        return new OperationNode(
                operationNode.getOperator(),
                operationNode.getLeftOperand().accept(this),
                operationNode.getRightOperand().accept(this));
    }

    @Override
    public ServerExpressionNode visitFunctionCall(FunctionCallNode functionCall) {
        // Copy as child nodes may be replaced
        List<ServerExpressionNode> arguments = functionCall.getArguments().stream().map(child -> child.accept(this)).collect(Collectors.toList());
        return new FunctionCallNode(functionCall.getFunction(), arguments);
    }

    @Override
    public ServerExpressionNode visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return new TimeBasedAggregationNode(
                        aggregationNode.getAggregatedExpression().accept(this),
                        aggregationNode.getFunction(),
                        aggregationNode.getIntervalLength());
    }

    private class Context {
        final VirtualRequirementNode node;
        final MeteringService meteringService;
        final MeterActivation meterActivation;

        private Context(VirtualRequirementNode node, MeteringService meteringService, MeterActivation meterActivation) {
            this.node = node;
            this.meteringService = meteringService;
            this.meterActivation = meterActivation;
        }

        Optional<BigDecimal> getMultiplier(MultiplierType.StandardType type) {
            return this.meterActivation.getMultiplier(this.meteringService.getMultiplierType(type));
        }

        ServerExpressionNode multiply(BigDecimal multiplier) {
            return new OperationNode(
                    Operator.MULTIPLY,
                    new NumericalConstantNode(multiplier),
                    this.node);
        }

    }

    private enum RequirementNodeReplacer {
        /**
         * Applies voltage multiplier and will replace
         * the node with an expression that applies
         * the voltage multiplier from the meter activation
         * to the node.
         */
        VT {
            @Override
            ServerExpressionNode apply(Context context) {
                return context
                    .getMultiplier(MultiplierType.StandardType.VT)
                    .map(context::multiply)
                    .orElse(context.node);
            }
        },

        /**
         * Applies current multiplier and will replace
         * the node with an expression that applies
         * the current multiplier from the meter activation
         * to the node.
         */
        CT {
            @Override
            ServerExpressionNode apply(Context context) {
                return context
                        .getMultiplier(MultiplierType.StandardType.CT)
                        .map(context::multiply)
                        .orElse(context.node);
            }
        },

        /**
         * Applies transformer multiplier and will replace
         * the node with an expression that applies
         * the transformer multiplier to the node.
         * The transformer multiplier is either taken
         * from the meter activation (if defined)
         * or calculated as CT/VT if the latter two
         * are defined in the meter activation.
         */
        TRANSFORMER {
            @Override
            ServerExpressionNode apply(Context context) {
                return context
                        .getMultiplier(MultiplierType.StandardType.Transformer)
                        .map(context::multiply)
                        .orElseGet(() -> CALCULATED_TRANSFORMER.apply(context));
            }
        },

        /**
         * Applies CT and VT multipliers as a transformer multiplier
         * (i.e. multiplier = CT/VT) and will replace
         * the node with an expression that applies the calculated
         * multiplier to the node.
         */
        CALCULATED_TRANSFORMER {
            @Override
            ServerExpressionNode apply(Context context) {
                Optional<BigDecimal> ctMultiplier = context.getMultiplier(MultiplierType.StandardType.CT);
                Optional<BigDecimal> vtMultiplier = context.getMultiplier(MultiplierType.StandardType.VT);
                if (ctMultiplier.isPresent() && vtMultiplier.isPresent()) {
                    return new OperationNode(
                            Operator.MULTIPLY,
                            new OperationNode(
                                    Operator.DIVIDE,
                                    new NumericalConstantNode(ctMultiplier.get()),
                                    new NumericalConstantNode(vtMultiplier.get())),
                            context.node);
                } else {
                    return context.node;
                }
            }
        },

        /**
         * Applies to replacement, i.e. simply returns the same node.
         */
        NONE {
            @Override
            ServerExpressionNode apply(Context context) {
                return context.node;
            }
        };

        abstract ServerExpressionNode apply(Context context);

        static RequirementNodeReplacer from(ReadingType readingType) {
            switch (readingType.getMeasurementKind()) {
                case ACVOLTAGEPEAK: // Intended fall-through
                case DCVOLTAGE:     // Intended fall-through
                case VOLTAGE: {
                    return VT;
                }
                case CURRENT: {
                    return CT;
                }
                case ENERGY: {
                    return TRANSFORMER;
                }
                default: {
                    return NONE;
                }
            }
        }

    }

}