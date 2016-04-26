package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.MetrologyContract;
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
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.search.rest.SearchCriteriaVisualizationInfo;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetrologyConfigurationInfoFactory {

    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public MetrologyConfigurationInfoFactory(Thesaurus thesaurus, CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        this.thesaurus = thesaurus;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
    }

    public MetrologyConfigurationInfo asInfo(UsagePointMetrologyConfiguration metrologyConfiguration) {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = metrologyConfiguration.getId();
        info.name = metrologyConfiguration.getName();
        info.description = metrologyConfiguration.getDescription();
        info.status = asInfo(metrologyConfiguration.getStatus());
        info.serviceCategory = asInfo(metrologyConfiguration.getServiceCategory());
        info.version = metrologyConfiguration.getVersion();
        info.meterRoles = metrologyConfiguration.getMeterRoles().stream().map(this::asInfo).collect(Collectors.toList());
        info.purposes = metrologyConfiguration.getContracts().stream().map(this::asInfo).collect(Collectors.toList());
        info.usagePointRequirements = metrologyConfiguration.getUsagePointRequirements()
                .stream()
                .map(requirement -> SearchCriteriaVisualizationInfo.from(requirement.getSearchableProperty(), requirement.toValueBean()))
                .collect(Collectors.toList());
        return info;
    }

    public MetrologyConfigurationInfo asDetailedInfo(UsagePointMetrologyConfiguration meterConfiguration) {
        MetrologyConfigurationInfo info = asInfo(meterConfiguration);
        info.metrologyContracts = meterConfiguration.getContracts().stream().map(this::asDetailedInfo).collect(Collectors.toList());

        info.customPropertySets = meterConfiguration.getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(this.customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                .collect(Collectors.toList());
        return info;
    }

    private IdWithNameInfo asInfo(MetrologyConfigurationStatus status) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = status.getId();
        info.name = thesaurus.getFormat(MetrologyConfigurationStatusTranslationKeys.getTranslatedName(status)).format();
        return info;
    }

    private IdWithNameInfo asInfo(ServiceCategory serviceCategory) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = serviceCategory.getKind().name();
        info.name = serviceCategory.getName();
        return info;
    }

    private IdWithNameInfo asInfo(MeterRole meterRole) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = meterRole.getKey();
        info.name = meterRole.getDisplayName();
        return info;
    }

    private IdWithNameInfo asInfo(MetrologyContract metrologyContract) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = metrologyContract.getMetrologyPurpose().getId();
        info.name = metrologyContract.getMetrologyPurpose().getName();
        return info;
    }

    private MetrologyContractInfo asDetailedInfo(MetrologyContract metrologyContract) {
        MetrologyContractInfo info = new MetrologyContractInfo();
        info.id = metrologyContract.getMetrologyPurpose().getId();
        info.name = metrologyContract.getMetrologyPurpose().getName();
        info.mandatory = metrologyContract.isMandatory();
        info.readingTypeDeliverables = metrologyContract.getDeliverables().stream().map(this::asInfo).collect(Collectors.toList());
        return info;
    }

    private ReadingTypeDeliverablesInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        ReadingTypeDeliverablesInfo info = new ReadingTypeDeliverablesInfo();
        info.id = readingTypeDeliverable.getId();
        info.name = readingTypeDeliverable.getName();
        info.readingType = readingTypeDeliverable.getReadingType() != null ? new ReadingTypeInfo(readingTypeDeliverable.getReadingType()) : null;
        info.formula = readingTypeDeliverable.getFormula() != null ? asInfo(readingTypeDeliverable.getFormula()) : null;
        return info;
    }

    private FormulaInfo asInfo(Formula formula) {
        FormulaInfo info = new FormulaInfo();
        info.description = formula.getDescription();
        info.readingTypeRequirements = asInfoList(formula.getExpressionNode());
        info.customProperties = Collections.emptyList(); //not supported yet
        return info;
    }


    private List<ReadingTypeRequirementsInfo> asInfoList(ExpressionNode expressionNode) {

        ReadingTypeVisitor readingTypeVisitor = new ReadingTypeVisitor();
        expressionNode.accept(readingTypeVisitor);
        return readingTypeVisitor.readingTypeRequirementNodes.stream().map(e -> asInfo(e)).collect(Collectors.toList());
    }

    private ReadingTypeRequirementsInfo asInfo(ReadingTypeRequirementNode requirementNode) {
        ReadingTypeRequirement requirement = requirementNode.getReadingTypeRequirement();
        ReadingTypeRequirementsInfo info = new ReadingTypeRequirementsInfo();
        info.meterRole = ((UsagePointMetrologyConfiguration) requirement.getMetrologyConfiguration())
                .getMeterRoleFor(requirement)
                .map(this::asInfo)
                .orElse(null);
        if (requirement instanceof FullySpecifiedReadingTypeRequirement) {
            FullySpecifiedReadingTypeRequirement fullySpecifiedReadingTypeRequirement = (FullySpecifiedReadingTypeRequirement) requirement;
            info.readingType = new ReadingTypeInfo(fullySpecifiedReadingTypeRequirement.getReadingType());
            info.type = "fullySpecified";
        } else if (requirement instanceof PartiallySpecifiedReadingTypeRequirement) {
            PartiallySpecifiedReadingTypeRequirement partiallySpecified = (PartiallySpecifiedReadingTypeRequirement) requirement;
            info.type = "partiallySpecified";
            info.readingTypePattern = new ReadingTypePatternInfo();
            info.readingTypePattern.value = requirementNode.toString() + ", " + partiallySpecified.getDescription();
            info.readingTypePattern.attributes = new ReadingTypePatternAttributeInfo();
            info.readingTypePattern.attributes.multiplier = partiallySpecified
                    .getAttributeValue(ReadingTypeTemplateAttributeName.METRIC_MULTIPLIER)
                    .map(Collections::singletonList).orElse(null);
            info.readingTypePattern.attributes.accumulation = partiallySpecified
                    .getAttributeValue(ReadingTypeTemplateAttributeName.ACCUMULATION)
                    .map(Collections::singletonList).orElse(null);
            info.readingTypePattern.attributes.timePeriod =
                    Stream.of(partiallySpecified.getAttributeValue(ReadingTypeTemplateAttributeName.MACRO_PERIOD),
                            partiallySpecified.getAttributeValue(ReadingTypeTemplateAttributeName.ACCUMULATION))
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

    private class ReadingTypeVisitor implements ExpressionNode.Visitor<Void> {

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