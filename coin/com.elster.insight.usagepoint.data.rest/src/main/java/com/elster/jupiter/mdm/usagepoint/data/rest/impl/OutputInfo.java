package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.mdm.common.rest.TimeDurationInfo;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.TimeDuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OutputInfo {
    public long id;
    public String name;
    public TimeDurationInfo interval;
    public ReadingTypeInfo readingType;
    public long version;
    public String flowUnit;
    public FormulaInfo formula;

    public OutputInfo() {

    }

    public static OutputInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        OutputInfo outputInfo = new OutputInfo();
        TimeDuration timeDuration = null;
        ReadingType readingType = readingTypeDeliverable.getReadingType();
        MacroPeriod macroPeriod = readingType.getMacroPeriod();
        TimeAttribute measuringPeriod = readingType.getMeasuringPeriod();
        outputInfo.id = readingTypeDeliverable.getId();
        outputInfo.name = readingTypeDeliverable.getName();
        outputInfo.readingType = new ReadingTypeInfo(readingType);
        outputInfo.formula = readingTypeDeliverable.getFormula() != null ? asInfo(readingTypeDeliverable.getFormula()) : null;
        if (!measuringPeriod.equals(TimeAttribute.NOTAPPLICABLE)) {
            timeDuration = TimeDuration.minutes(measuringPeriod.getMinutes());
        } else if (macroPeriod.equals(MacroPeriod.DAILY)) {
            timeDuration = TimeDuration.days(1);
        } else if (macroPeriod.equals(MacroPeriod.MONTHLY)) {
            timeDuration = TimeDuration.months(1);
        } else if (macroPeriod.equals(MacroPeriod.WEEKLYS)) {
            timeDuration = TimeDuration.weeks(1);
        }
        outputInfo.interval = new TimeDurationInfo(timeDuration);
        outputInfo.flowUnit = ReadingTypeUnitConversion.isFlowUnit(readingType.getUnit().name()) ? "flow" : "volume";
        return outputInfo;
    }

    public static FormulaInfo asInfo(Formula formula) {
        FormulaInfo info = new FormulaInfo();
        info.description = formula.getDescription();
        info.readingTypeRequirements = asInfoList(formula.getExpressionNode());
        info.customProperties = Collections.emptyList(); // not supported yet
        return info;
    }

    public static IdWithNameInfo asInfo(MeterRole meterRole) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = meterRole.getKey();
        info.name = meterRole.getDisplayName();
        return info;
    }


    public static List<ReadingTypeRequirementsInfo> asInfoList(ExpressionNode expressionNode) {

        ReadingTypeVisitor readingTypeVisitor = new ReadingTypeVisitor();
        expressionNode.accept(readingTypeVisitor);
        return readingTypeVisitor.readingTypeRequirementNodes.stream().map(e -> asInfo(e.getReadingTypeRequirement())).collect(Collectors.toList());
    }

    public static ReadingTypeRequirementsInfo asInfo(ReadingTypeRequirement requirement) {
        ReadingTypeRequirementsInfo info = new ReadingTypeRequirementsInfo();
        info.meterRole = asInfo(((UsagePointMetrologyConfiguration) requirement.getMetrologyConfiguration())
                .getMeterRoleFor(requirement).get());
        if (requirement instanceof FullySpecifiedReadingTypeRequirement) {
            FullySpecifiedReadingTypeRequirement fullySpecified = (FullySpecifiedReadingTypeRequirement) requirement;
            info.readingType = new ReadingTypeInfo(fullySpecified.getReadingType());
            info.type = "fullySpecified";
        }
//        else if(requirement instanceof PartiallySpecifiedReadingTypeRequirement){
//            PartiallySpecifiedReadingTypeRequirement partiallySpecified = (PartiallySpecifiedReadingTypeRequirement) requirement;
//            info.type = "partiallySpecified";
//            info.readingTypePattern = new ReadingTypePatternInfo();
//            info.readingTypePattern.value = partiallySpecified.getDescription();
//            info.readingTypePattern.attributes = new ReadingTypePatternAttributeInfo();
//            info.readingTypePattern.attributes.multiplier = partiallySpecified
//                    .getAttributeValue(ReadingTypeTemplateAttributeName.METRIC_MULTIPLIER)
//                    .map(Collections::singletonList).orElse(null);
//            info.readingTypePattern.attributes.accumulation = partiallySpecified
//                    .getAttributeValue(ReadingTypeTemplateAttributeName.ACCUMULATION)
//                    .map(Collections::singletonList).orElse(null);
//            info.readingTypePattern.attributes.timePeriod =
//                    Stream.of(partiallySpecified.getAttributeValue(ReadingTypeTemplateAttributeName.MACRO_PERIOD),
//                            partiallySpecified.getAttributeValue(ReadingTypeTemplateAttributeName.ACCUMULATION))
//                            .flatMap(com.elster.jupiter.util.streams.Functions.asStream()).findFirst()
//                            .map(Collections::singletonList).orElse(null);
//            List<String> unitValues = partiallySpecified.getAttributeValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE)
//                    .stream().flatMap(com.elster.jupiter.util.streams.Functions.asStream()).collect(Collectors.toList());
//            if (!unitValues.isEmpty() && unitValues.size() > 1) {
//                info.readingTypePattern.attributes.unit = unitValues;
//            }
//        }
        return info;
    }

    public static class ReadingTypeVisitor implements ExpressionNode.Visitor<Void> {

        private List<ReadingTypeRequirementNode> readingTypeRequirementNodes = new ArrayList<>();

        @Override
        public Void visitConstant(ConstantNode constant) {
            return null;
        }

        @Override
        public Void visitNull(NullNode nullNode) {
            return null;
        }

        @Override
        public Void visitRequirement(ReadingTypeRequirementNode requirement) {
            readingTypeRequirementNodes.add(requirement);
            return null;
        }

        @Override
        public Void visitDeliverable(ReadingTypeDeliverableNode deliverable) {
            return null;
        }

        @Override
        public Void visitOperation(OperationNode operationNode) {
            operationNode.getChildren().forEach(n -> n.accept(this));
            return null;
        }

        @Override
        public Void visitFunctionCall(FunctionCallNode functionCall) {
            functionCall.getChildren().forEach(n -> n.accept(this));
            return null;
        }
    }
}
