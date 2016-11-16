package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetrologyConfigurationInfo {
    public Long id;
    public String name;
    public Long version;
    public Instant activationTime;
    public List<CustomPropertySetInfo> customPropertySets = new ArrayList<>();
    public IdWithNameInfo status;
    public List<MeterRoleInfo> meterRoles;
    public List<PurposeInfo> purposes;

    @JsonIgnore
    public Thesaurus thesaurus;
    @JsonIgnore
    public Clock clock;

    public MetrologyConfigurationInfo() {
    }

    public MetrologyConfigurationInfo(UsagePointMetrologyConfiguration metrologyConfiguration, UsagePoint usagePoint, Thesaurus thesaurus, Clock clock, ReadingTypeDeliverableFactory readingTypeDeliverableFactory) {
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.id = metrologyConfiguration.getId();
        this.name = metrologyConfiguration.getName();
        this.version = metrologyConfiguration.getVersion();
        this.activationTime = usagePoint.getInstallationTime();
        this.meterRoles = metrologyConfiguration.getMeterRoles()
                .stream()
                .map(mr -> asDetailedInfo(mr, metrologyConfiguration, usagePoint))
                .collect(Collectors.toList());
        this.purposes = metrologyConfiguration.getContracts()
                .stream()
                .map(c -> asDetailedInfo(c, metrologyConfiguration, usagePoint, readingTypeDeliverableFactory))
                .sorted(Comparator.comparing(info -> info.name))
                .collect(Collectors.toList());
        this.status = asInfo();
    }

    private IdWithNameInfo asInfo() {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = this.purposes
                .stream()
                .filter(p -> p.active)
                .allMatch(purposeInfo -> purposeInfo.status.id.equals("complete")) ? "complete" : "incomplete";
        info.name = info.id.equals("incomplete") ? this.thesaurus.getFormat(DefaultTranslationKey.INCOMPLETE).format() :
                this.thesaurus.getFormat(DefaultTranslationKey.COMPLETE).format();
        return info;
    }

    private MeterRoleInfo asDetailedInfo(MeterRole meterRole, UsagePointMetrologyConfiguration metrologyConfiguration, UsagePoint usagePoint) {
        MeterRoleInfo info = new MeterRoleInfo();
        info.id = meterRole.getKey();
        info.name = meterRole.getDisplayName();
        MeterActivation meterActivation = !usagePoint.getMeterActivations(meterRole).isEmpty() ? usagePoint.getMeterActivations(meterRole)
                .stream()
                .filter(meterActivationToCheck -> meterActivationToCheck.getEnd() == null)
                .findFirst()
                .orElse(null) : null;
        Meter meter = meterActivation != null ? meterActivation.getMeter().get() : null;
        info.mRID = meterActivation != null ? meterActivation.getMeter().get().getMRID() : null;
        info.url = meter != null ? meter.getHeadEndInterface()
                .map(he -> he.getURLForEndDevice(meter)
                        .map(URL::toString)
                        .orElse(null))
                .orElse(null) : null;
        info.activationTime = meterActivation != null ? this.activationTime : null;
        List<ReadingTypeDeliverable> readingTypeDeliverables = metrologyConfiguration.getContracts()
                .stream()
                .filter(MetrologyContract::isMandatory)
                .flatMap(c -> c.getDeliverables().stream())
                .collect(Collectors.toList());
        List<MeterRole> meterRolesList = readingTypeDeliverables
                .stream()
                .flatMap(d -> getMeterRolesFromReadingTypeDeliverable(d).stream())
                .collect(Collectors.toList());
        info.required = meterRolesList
                .stream()
                .anyMatch(mr -> mr.getKey().equals(meterRole.getKey()));
        return info;
    }

    private List<MeterRole> getMeterRolesFromReadingTypeDeliverable(ReadingTypeDeliverable readingTypeDeliverable) {
        List<MeterRole> meterRolesList = new ArrayList<>();
        ReadingTypeVisitor readingTypeVisitor = new ReadingTypeVisitor();
        readingTypeDeliverable.getFormula().getExpressionNode().accept(readingTypeVisitor);
        readingTypeVisitor.readingTypeRequirementNodes
                .stream()
                .map(ReadingTypeRequirementNode::getReadingTypeRequirement)
                .map(requirement -> ((UsagePointMetrologyConfiguration) requirement.getMetrologyConfiguration()).getMeterRoleFor(requirement))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(meterRolesList::add);
        return meterRolesList;
    }

    private PurposeInfo asDetailedInfo(MetrologyContract metrologyContract, UsagePointMetrologyConfiguration metrologyConfiguration,
                                       UsagePoint usagePoint, ReadingTypeDeliverableFactory readingTypeDeliverableFactory) {
        PurposeInfo info = new PurposeInfo();
        info.id = metrologyContract.getId();
        info.name = metrologyContract.getMetrologyPurpose().getName();
        info.description = metrologyContract.getMetrologyPurpose().getDescription();
        info.required = metrologyContract.isMandatory();
        info.active = usagePoint.getEffectiveMetrologyConfiguration(clock.instant()).flatMap(mc -> mc.getChannelsContainer(metrologyContract, clock.instant())).isPresent();
        info.meterRoles = asInfoList(metrologyConfiguration, usagePoint);
        IdWithNameInfo metrologyContractStatus = new IdWithNameInfo();
        metrologyContractStatus.id = metrologyContract.getStatus(usagePoint).getKey().equals("COMPLETE") ? "complete" : "incomplete";
        metrologyContractStatus.name = metrologyContract.getStatus(usagePoint).getName();
        info.status = metrologyContractStatus;
        info.readingTypeDeliverables = metrologyContract.getDeliverables().stream().map(readingTypeDeliverableFactory::asInfo).collect(Collectors.toList());
        return info;
    }

    private List<MeterRoleInfo> asInfoList(UsagePointMetrologyConfiguration metrologyConfiguration, UsagePoint usagePoint) {
        List<ReadingTypeDeliverable> readingTypeDeliverables = metrologyConfiguration.getContracts()
                .stream()
                .flatMap(c -> c.getDeliverables().stream())
                .collect(Collectors.toList());
        return readingTypeDeliverables
                .stream()
                .flatMap(d -> getMeterRolesFromReadingTypeDeliverable(d).stream())
                .distinct()
                .map(meterRole -> asInfo(meterRole, usagePoint))
                .collect(Collectors.toList());
    }

    private MeterRoleInfo asInfo(MeterRole meterRole, UsagePoint usagePoint) {
        MeterRoleInfo info = new MeterRoleInfo();
        info.name = meterRole.getDisplayName();
        MeterActivation meterActivation = !usagePoint.getMeterActivations(meterRole).isEmpty() ? usagePoint.getMeterActivations(meterRole)
                .stream()
                .filter(meterActivationToCheck -> meterActivationToCheck.getEnd() == null)
                .findFirst()
                .orElse(null) : null;
        Meter meter = meterActivation != null ? meterActivation.getMeter().get() : null;
        info.mRID = meterActivation != null ? meterActivation.getMeter().get().getMRID() : null;
        info.url = meter != null ? meter.getHeadEndInterface()
                .map(he -> he.getURLForEndDevice(meter)
                        .map(URL::toString)
                        .orElse(null))
                .orElse(null) : null;
        return info;
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

        @Override
        public Void visitNull(NullNode nullNode) {
            return null;
        }
    }

    public MetrologyConfigurationInfo(UsagePointMetrologyConfiguration usagePointMetrologyConfiguration, List<CustomPropertySetInfo> customPropertySets) {
        this.id = usagePointMetrologyConfiguration.getId();
        this.name = usagePointMetrologyConfiguration.getName();
        this.version = usagePointMetrologyConfiguration.getVersion();
        this.customPropertySets = customPropertySets;
    }

    public MetrologyConfigurationInfo(UsagePointMetrologyConfiguration usagePointMetrologyConfiguration) {
        this.id = usagePointMetrologyConfiguration.getId();
        this.name = usagePointMetrologyConfiguration.getName();
        this.version = usagePointMetrologyConfiguration.getVersion();
        this.meterRoles = usagePointMetrologyConfiguration.getMeterRoles()
                .stream()
                .map(MeterRoleInfo::new)
                .collect(Collectors.toList());
    }

}
