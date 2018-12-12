/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Dimension;

import java.math.BigDecimal;
import java.util.EnumSet;
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
    private final MeterActivationSet meterActivationSet;

    ApplyCurrentAndOrVoltageTransformer(MeteringService meteringService, MeterActivationSet meterActivationSet) {
        this.meteringService = meteringService;
        this.meterActivationSet = meterActivationSet;
    }

    @Override
    public ServerExpressionNode visitVirtualRequirement(VirtualRequirementNode requirement) {
        ReadingType readingType = requirement.getPreferredChannel().getMainReadingType();
        return RequirementNodeReplacer
                .from(readingType)
                .to(this.getTargetReadingType(requirement, requirement.getDeliverableReadingType()))
                .apply(new Context(
                        requirement,
                        this.meteringService,
                        this.meterActivationSet));
    }

    private VirtualReadingType getTargetReadingType(VirtualRequirementNode requirementNode, ReadingType deliverableReadingType) {
        VirtualReadingType targetReadingType = requirementNode.getTargetReadingType();
        if (this.readingTypeIsElectricity(deliverableReadingType)) {
            targetReadingType = targetReadingType.withCommondity(deliverableReadingType.getCommodity());
        }
        return targetReadingType;
    }

    private boolean readingTypeIsElectricity(ReadingType readingType) {
        Commodity commodity = readingType.getCommodity();
        return EnumSet.of(
                    Commodity.ELECTRICITY_PRIMARY_METERED,
                    Commodity.ELECTRICITY_SECONDARY_METERED)
                .contains(commodity);
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
    public ServerExpressionNode visitProperty(CustomPropertyNode property) {
        // No replacement
        return property;
    }

    @Override
    public ServerExpressionNode visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
        // No replacement
        return slp;
    }

    @Override
    public ServerExpressionNode visitNull(NullNode nullNode) {
        // No replacement
        return nullNode;
    }

    @Override
    public ServerExpressionNode visitSqlFragment(SqlFragmentNode variable) {
        // No replacement
        return variable;
    }

    @Override
    public ServerExpressionNode visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        // No replacement
        return deliverable;
    }

    @Override
    public ServerExpressionNode visitUnitConversion(UnitConversionNode unitConversionNode) {
        return new UnitConversionNode(
                unitConversionNode.getExpressionNode().accept(this),
                unitConversionNode.getSourceReadingType(),
                unitConversionNode.getTargetReadingType());
    }

    @Override
    public ServerExpressionNode visitOperation(OperationNode operationNode) {
        // Copy as child nodes may be replaced
        Operator operator = operationNode.getOperator();
        ServerExpressionNode operand1 = operationNode.getLeftOperand().accept(this);
        ServerExpressionNode operand2 = operationNode.getRightOperand().accept(this);
        if (Operator.SAFE_DIVIDE.equals(operator)) {
            return new OperationNode(
                    operator,
                    operationNode.getIntermediateDimension(),
                    operand1,
                    operand2,
                    operationNode.getSafeDivisor().accept(this));
        } else {
            return new OperationNode(
                    operator,
                    operationNode.getIntermediateDimension(),
                    operand1,
                    operand2);
        }
    }

    @Override
    public ServerExpressionNode visitFunctionCall(FunctionCallNode functionCall) {
        // Copy as child nodes may be replaced
        List<ServerExpressionNode> arguments = functionCall.getArguments().stream().map(child -> child.accept(this)).collect(Collectors.toList());
        return new FunctionCallNode(functionCall.getFunction(), IntermediateDimension.of(Dimension.DIMENSIONLESS), arguments);
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
        final MeterActivationSet meterActivationSet;

        private Context(VirtualRequirementNode node, MeteringService meteringService, MeterActivationSet meterActivationSet) {
            this.node = node;
            this.meteringService = meteringService;
            this.meterActivationSet = meterActivationSet;
        }

        Optional<BigDecimal> getMultiplier(MultiplierType.StandardType type) {
            return this.meterActivationSet.getMultiplier(this.node.getRequirement(), this.meteringService.getMultiplierType(type));
        }

        ServerExpressionNode multiply(BigDecimal multiplier) {
            return Operator.MULTIPLY.node(multiplier, this.node);
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
                    return Operator.MULTIPLY.node(
                            Operator.DIVIDE.node(ctMultiplier.get(), vtMultiplier.get()),
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

        static RequirementNodeReplacerTargetReadingType from(ReadingType readingType) {
            if (Commodity.ELECTRICITY_SECONDARY_METERED.equals(readingType.getCommodity())) {
                return from(readingType.getMeasurementKind());
            } else {
                return RequirementNodeReplacerTargetReadingType.NONE;
            }
        }

        private static RequirementNodeReplacerTargetReadingType from(MeasurementKind measurementKind) {
            switch (measurementKind) {
                case ACVOLTAGEPEAK: // Intended fall-through
                case DCVOLTAGE:     // Intended fall-through
                case VOLTAGE: {
                    return RequirementNodeReplacerTargetReadingType.VT;
                }
                case CURRENT: {
                    return RequirementNodeReplacerTargetReadingType.CT;
                }
                case ENERGY: {
                    return RequirementNodeReplacerTargetReadingType.TRANSFORMER;
                }
                default: {
                    return RequirementNodeReplacerTargetReadingType.NONE;
                }
            }
        }

    }

    private enum RequirementNodeReplacerTargetReadingType {
        TRANSFORMER {
            @Override
            RequirementNodeReplacer forPrimaryMetered() {
                return RequirementNodeReplacer.TRANSFORMER;
            }
        },
        VT {
            @Override
            RequirementNodeReplacer forPrimaryMetered() {
                return RequirementNodeReplacer.VT;
            }
        },
        CT {
            @Override
            RequirementNodeReplacer forPrimaryMetered() {
                return RequirementNodeReplacer.CT;
            }
        },
        NONE {
            @Override
            RequirementNodeReplacer to(VirtualReadingType targetReadingType) {
                return RequirementNodeReplacer.NONE;
            }

            @Override
            RequirementNodeReplacer forPrimaryMetered() {
                return RequirementNodeReplacer.NONE;
            }
        };

        RequirementNodeReplacer to(VirtualReadingType targetReadingType) {
            if (targetReadingType.isPrimaryMetered()) {
                return this.forPrimaryMetered();
            } else {
                return RequirementNodeReplacer.NONE;
            }
        }

        abstract RequirementNodeReplacer forPrimaryMetered();

    }
}