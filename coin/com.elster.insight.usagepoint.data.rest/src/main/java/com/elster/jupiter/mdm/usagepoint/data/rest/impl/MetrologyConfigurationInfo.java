/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
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
    public Instant endDate;
    public List<CustomPropertySetInfo> customPropertySets = new ArrayList<>();
    public IdWithNameInfo status;
    public List<MeterRoleInfo> meterRoles;
    public List<PurposeInfo> purposes;
    public boolean requiresCalendar;

    @JsonIgnore
    public Thesaurus thesaurus;
    @JsonIgnore
    public Clock clock;

    public MetrologyConfigurationInfo() {
    }

    public MetrologyConfigurationInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint, UsagePoint usagePoint, Thesaurus thesaurus, Clock clock, ReadingTypeDeliverableFactory readingTypeDeliverableFactory) {
        UsagePointMetrologyConfiguration metrologyConfiguration = effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration();
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.id = metrologyConfiguration.getId();
        this.name = metrologyConfiguration.getName();
        this.version = metrologyConfiguration.getVersion();
        this.activationTime = effectiveMetrologyConfigurationOnUsagePoint.getStart();
        this.endDate = effectiveMetrologyConfigurationOnUsagePoint.getEnd();
        this.meterRoles = metrologyConfiguration.getMeterRoles()
                .stream()
                .map(mr -> asDetailedMeterRoleInfo(mr, metrologyConfiguration, usagePoint))
                .collect(Collectors.toList());
        this.purposes = metrologyConfiguration.getContracts()
                .stream()
                .map(c -> asDetailedPurposeInfo(c, usagePoint, readingTypeDeliverableFactory, effectiveMetrologyConfigurationOnUsagePoint))
                .sorted(Comparator.comparing(info -> info.name))
                .collect(Collectors.toList());
        this.status = statusInfo();
        this.requiresCalendar = metrologyConfiguration.requiresCalendarOnUsagePoint();
    }

    private IdWithNameInfo statusInfo() {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = this.purposes
                .stream()
                .filter(p -> p.active)
                .allMatch(purposeInfo -> purposeInfo.status.id.equals("complete")) ? "complete" : "incomplete";
        info.name = info.id.equals("incomplete") ? this.thesaurus.getFormat(DefaultTranslationKey.INCOMPLETE).format() :
                this.thesaurus.getFormat(DefaultTranslationKey.COMPLETE).format();
        return info;
    }

    private MeterRoleInfo asDetailedMeterRoleInfo(MeterRole meterRole, UsagePointMetrologyConfiguration metrologyConfiguration, UsagePoint usagePoint) {
        MeterRoleInfo info = asMeterRoleInfo(meterRole, usagePoint);
        info.id = meterRole.getKey();
        info.required = metrologyConfiguration.getContracts()
                .stream()
                .filter(MetrologyContract::isMandatory)
                .flatMap(c -> c.getDeliverables().stream())
                .flatMap(d -> getMeterRolesFromReadingTypeDeliverable(d).stream())
                .anyMatch(mr -> mr.getKey().equals(meterRole.getKey()));
        return info;
    }

    private static List<MeterRole> getMeterRolesFromReadingTypeDeliverable(ReadingTypeDeliverable readingTypeDeliverable) {
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

    private PurposeInfo asDetailedPurposeInfo(MetrologyContract metrologyContract, UsagePoint usagePoint,
                                              ReadingTypeDeliverableFactory readingTypeDeliverableFactory,
                                              EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint) {
        PurposeInfo info = new PurposeInfo();
        info.id = metrologyContract.getId();
        info.name = metrologyContract.getMetrologyPurpose().getName();
        info.description = metrologyContract.getMetrologyPurpose().getDescription();
        info.required = metrologyContract.isMandatory();
        info.active = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .flatMap(mc -> mc.getChannelsContainer(metrologyContract, clock.instant()))
                .isPresent();
        info.meterRoles = asMeterRoleInfoList(metrologyContract, usagePoint);
        IdWithNameInfo metrologyContractStatus = new IdWithNameInfo();
        metrologyContractStatus.id = effectiveMetrologyConfigurationOnUsagePoint.isComplete(metrologyContract) ? "complete" : "incomplete";
        metrologyContractStatus.name = effectiveMetrologyConfigurationOnUsagePoint.isComplete(metrologyContract) ?
                thesaurus.getFormat(DefaultMetrologyPurpose.Translation.METROLOGY_CONTRACT_STATUS_COMPLETE).format()
                : thesaurus.getFormat(DefaultMetrologyPurpose.Translation.METROLOGY_CONTRACT_STATUS_INCOMPLETE).format();
        info.status = metrologyContractStatus;
        info.readingTypeDeliverables = metrologyContract.getDeliverables()
                .stream()
                .sorted(Comparator.comparing(ReadingTypeDeliverable::getName))
                .map(readingTypeDeliverableFactory::asInfo)
                .collect(Collectors.toList());
        info.eventNames = new ArrayList<>();
        List<Long> longs = metrologyContract.getDeliverables()
                .stream()
                .map(readingTypeDeliverable -> (long) readingTypeDeliverable.getReadingType().getTou())
                .collect(Collectors.toList());

        List<Event> eventList = metrologyContract.getMetrologyConfiguration().getEventSets().stream()
                .flatMap(eventSet -> eventSet.getEvents().stream()).collect(Collectors.toList());


        eventList.stream().filter(event -> longs.contains(event.getCode())).forEach(event2 -> info.eventNames.add(info.eventNames.size(), event2.getName()));
        return info;
    }

    private List<MeterRoleInfo> asMeterRoleInfoList(MetrologyContract metrologyContract, UsagePoint usagePoint) {
        return metrologyContract.getDeliverables().stream()
                .flatMap(d -> getMeterRolesFromReadingTypeDeliverable(d).stream())
                .distinct()
                .map(meterRole -> asMeterRoleInfo(meterRole, usagePoint))
                .collect(Collectors.toList());
    }

    private MeterRoleInfo asMeterRoleInfo(MeterRole meterRole, UsagePoint usagePoint) {
        MeterRoleInfo info = new MeterRoleInfo();
        info.name = meterRole.getDisplayName();
        usagePoint.getMeterActivations(meterRole)
                .stream()
                .filter(meterActivationToCheck -> meterActivationToCheck.isEffectiveAt(clock.instant()))
                .findFirst()
                .ifPresent(meterActivation -> {
                    meterActivation.getMeter().ifPresent(meter -> {
                        info.meter = meter.getName();
                        info.url = meter.getHeadEndInterface()
                                .flatMap(he -> he.getURLForEndDevice(meter))
                                .map(URL::toString)
                                .orElse(null);
                        info.activationTime = meterActivation.getStart();
                    });
                });
        return info;
    }

    private static class ReadingTypeVisitor implements ExpressionNode.Visitor<Void> {

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
        this.requiresCalendar = usagePointMetrologyConfiguration.requiresCalendarOnUsagePoint();
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
