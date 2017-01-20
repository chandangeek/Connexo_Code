/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.rest.CustomPropertiesInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.FormulaInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverablesInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypePatternAttributeInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypePatternInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeRequirementsInfo;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverable.factory", immediate = true, service = {ReadingTypeDeliverableFactory.class}, property = "name=ReadingTypeDeliverableFactory")
public class ReadingTypeDeliverableFactoryImpl implements ReadingTypeDeliverableFactory {

    private volatile Thesaurus thesaurus;
    private ReadingTypeInfoFactory readingTypeInfoFactory;

    // For OSGi purposes
    public ReadingTypeDeliverableFactoryImpl() {
        super();
    }

    // For Testing purposes
    @Inject
    public ReadingTypeDeliverableFactoryImpl(Thesaurus thesaurus) {
        this();
        this.thesaurus = thesaurus;
        this.readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointConfigurationApplication.COMPONENT_NAME, Layer.REST);
        this.readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
    }

    @Override
    public ReadingTypeDeliverablesInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        return asInfo(readingTypeDeliverable, readingTypeDeliverable.getMetrologyConfiguration());
    }

    @Override
    public ReadingTypeDeliverablesInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable, MetrologyConfiguration metrologyConfiguration) {
        ReadingTypeDeliverablesInfo info = new ReadingTypeDeliverablesInfo();
        info.id = readingTypeDeliverable.getId();
        info.name = readingTypeDeliverable.getName();
        info.readingType = readingTypeDeliverable.getReadingType() != null ? readingTypeInfoFactory.from(readingTypeDeliverable.getReadingType()) : null;
        info.formula = readingTypeDeliverable.getFormula() != null ? asInfo(readingTypeDeliverable.getFormula(), metrologyConfiguration) : null;
        return info;
    }

    private FormulaInfo asInfo(Formula formula, MetrologyConfiguration metrologyConfiguration) {
        FormulaInfo info = new FormulaInfo();
        info.description = formula.getExpressionNode().accept(new FormulaDescriptionBuilder(this.thesaurus));
        info.readingTypeRequirements = asInfoList(formula.getExpressionNode(), metrologyConfiguration);
        CustomPropertyInfoFactory visitor = new CustomPropertyInfoFactory();
        formula.getExpressionNode().accept(visitor);
        info.customProperties = visitor.getCustomPropertiesInfos();
        return info;
    }

    private List<ReadingTypeRequirementsInfo> asInfoList(ExpressionNode expressionNode, MetrologyConfiguration metrologyConfiguration) {
        ReadingTypeVisitor readingTypeVisitor = new ReadingTypeVisitor();
        expressionNode.accept(readingTypeVisitor);
        return readingTypeVisitor.readingTypeRequirementNodes.stream().map(e -> asInfo(e, metrologyConfiguration)).collect(Collectors.toList());
    }

    private ReadingTypeRequirementsInfo asInfo(ReadingTypeRequirementNode requirementNode, MetrologyConfiguration metrologyConfiguration) {
        ReadingTypeRequirement requirement = requirementNode.getReadingTypeRequirement();
        ReadingTypeRequirementsInfo info = new ReadingTypeRequirementsInfo();
        info.meterRole = ((UsagePointMetrologyConfiguration) metrologyConfiguration)
                .getMeterRoleFor(requirement)
                .map(this::asInfo)
                .orElse(null);
        if (requirement instanceof FullySpecifiedReadingTypeRequirement) {
            FullySpecifiedReadingTypeRequirement fullySpecifiedReadingTypeRequirement = (FullySpecifiedReadingTypeRequirement) requirement;
            info.readingType = readingTypeInfoFactory.from(fullySpecifiedReadingTypeRequirement.getReadingType());
            info.type = "fullySpecified";
        } else if (requirement instanceof PartiallySpecifiedReadingTypeRequirement) {
            PartiallySpecifiedReadingTypeRequirement partiallySpecified = (PartiallySpecifiedReadingTypeRequirement) requirement;
            info.type = "partiallySpecified";
            info.readingTypePattern = new ReadingTypePatternInfo();
            info.readingTypePattern.value = requirementNode.accept(new FormulaDescriptionBuilder(this.thesaurus)) + ", " + partiallySpecified.getDescription();
            info.readingTypePattern.attributes = new ReadingTypePatternAttributeInfo();
            info.readingTypePattern.attributes.multiplier = partiallySpecified
                    .getAttributeValue(ReadingTypeTemplateAttributeName.METRIC_MULTIPLIER)
                    .map(Collections::singletonList).orElse(null);
            info.readingTypePattern.attributes.accumulation = partiallySpecified
                    .getAttributeValue(ReadingTypeTemplateAttributeName.ACCUMULATION)
                    .map(Collections::singletonList).orElse(null);
            info.readingTypePattern.attributes.timePeriod =
                    Stream.of(partiallySpecified.getAttributeValue(ReadingTypeTemplateAttributeName.MACRO_PERIOD),
                            partiallySpecified.getAttributeValue(ReadingTypeTemplateAttributeName.TIME))
                            .flatMap(com.elster.jupiter.util.streams.Functions.asStream()).findFirst()
                            .map(Collections::singletonList).orElse(null);
            List<String> unitValues = partiallySpecified.getAttributeValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE)
                    .stream().flatMap(com.elster.jupiter.util.streams.Functions.asStream()).collect(Collectors.toList());
            if (!unitValues.isEmpty() && unitValues.size() > 1) {
                info.readingTypePattern.attributes.unit = unitValues;
            }
        }
        return info;
    }

    private IdWithNameInfo asInfo(MeterRole meterRole) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = meterRole.getKey();
        info.name = meterRole.getDisplayName();
        return info;
    }

    private class CustomPropertyInfoFactory implements ExpressionNode.Visitor<Void> {

        private List<CustomPropertiesInfo> customProperties = new ArrayList<>();

        @Override
        public Void visitConstant(ConstantNode constant) {
            return null;
        }

        @Override
        public Void visitRequirement(ReadingTypeRequirementNode requirement) {
            return null;
        }

        @Override
        public Void visitDeliverable(ReadingTypeDeliverableNode deliverable) {
            return null;
        }

        public List<CustomPropertiesInfo> getCustomPropertiesInfos() {
            return Collections.unmodifiableList(customProperties);
        }

        @Override
        public Void visitProperty(CustomPropertyNode property) {
            CustomPropertiesInfo customProperty = new CustomPropertiesInfo();
            customProperty.name = property.getPropertySpec().getDisplayName();
            customProperty.key = property.getPropertySpec().getName();
            customProperty.customPropertySet = new IdWithNameInfo(property.getCustomPropertySet().getId(), property.getCustomPropertySet().getName());
            this.customProperties.add(customProperty);
            return null;
        }

        @Override
        public Void visitOperation(OperationNode operationNode) {
            operationNode.getChildren().forEach(n -> n.accept(this));
            return null;
        }

        @Override
        public Void visitFunctionCall(FunctionCallNode functionCall) {
            return null;
        }

        @Override
        public Void visitNull(NullNode nullNode) {
            return null;
        }
    }

    private class ReadingTypeVisitor implements ExpressionNode.Visitor<Void> {

        private List<ReadingTypeRequirementNode> readingTypeRequirementNodes = new ArrayList<>();

        @Override
        public Void visitConstant(ConstantNode constant) {
            return null;
        }

        @Override
        public Void visitProperty(CustomPropertyNode property) {
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