/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.dataquality.impl.calc.DataQualityKpiMemberType;
import com.elster.jupiter.dataquality.impl.calc.KpiType;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

@UniqueUsagePointGroupAndPurpose(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.USAGEPOINT_GROUP_AND_PURPOSE_MUST_BE_UNIQUE + "}")
public final class UsagePointDataQualityKpiImpl extends DataQualityKpiImpl implements UsagePointDataQualityKpi {

    static final String KPI_MEMBER_NAME_SUFFIX_SEPARATOR = ":";

    public enum Fields {

        USAGEPOINT_GROUP("usagePointGroup"),
        METROLOGY_PURPOSE("metrologyPurpose");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointGroup> usagePointGroup = ValueReference.absent();

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<MetrologyPurpose> metrologyPurpose = ValueReference.absent();

    @Inject
    public UsagePointDataQualityKpiImpl(DataModel dataModel, MeteringService meteringService, ValidationService validationService,
                                        EstimationService estimationService, MessageService messageService, TaskService taskService,
                                        KpiService kpiService, Clock clock) {
        super(dataModel, meteringService, validationService, estimationService, messageService, taskService, kpiService, clock);
    }

    UsagePointDataQualityKpiImpl init(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose, TemporalAmount calculationFrequency) {
        this.usagePointGroup.set(usagePointGroup);
        this.metrologyPurpose.set(metrologyPurpose);
        super.setFrequency(calculationFrequency);
        return this;
    }

    @Override
    public UsagePointGroup getUsagePointGroup() {
        return usagePointGroup.orNull();
    }

    @Override
    public MetrologyPurpose getMetrologyPurpose() {
        return metrologyPurpose.orNull();
    }

    @Override
    KpiType getKpiType() {
        return KpiType.USAGE_POINT_DATA_QUALITY_KPI;
    }

    @Override
    String getRecurrentTaskName() {
        return getKpiType().recurrentTaskName(getUsagePointGroup().getName() + "/" + getMetrologyPurpose().getName());
    }

    @Override
    QualityCodeSystem getQualityCodeSystem() {
        return QualityCodeSystem.MDM;
    }

    @Override
    public void makeObsolete() {
        this.metrologyPurpose.setNull();
        this.usagePointGroup.setNull();
        super.makeObsolete();
    }

    @Override
    public Map<Long, DataQualityKpiMember> updateMembers(Range<Instant> interval) {
        if (getKpiMembers().isEmpty()) {
            return createDataQualityKpiMembers(interval);
        }
        Set<Long> usagePointIdsInGroup = usagePointIdsInGroup();
        Map<Long, DataQualityKpiMember> dataQualityKpiMembersMap = usagePointIdsInKpiMembers();
        Set<Long> commonElements = intersection(usagePointIdsInGroup, dataQualityKpiMembersMap.keySet());

        // create kpis for new usage points in group
        usagePointIdsInGroup.stream()
                .filter(not(commonElements::contains))
                .forEach(usagePointId -> createDataQualityKpiMemberIfPurposeActive(usagePointId, interval));

        // update existing kpis with the new kpi members (in case new validators/estimators deployed)
        dataQualityKpiMembersMap.entrySet().stream()
                .filter(entry -> commonElements.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .forEach(super::updateKpiMemberIfNeeded);

        // remove kpis for usage points that disappeared from group
        Set<DataQualityKpiMember> obsoleteKpiMembers = dataQualityKpiMembersMap.entrySet().stream()
                .filter(not(entry -> commonElements.contains(entry.getKey())))
                .map(Map.Entry::getValue)
                .peek(DataQualityKpiMember::remove)
                .collect(Collectors.toSet());
        getKpiMembers().removeAll(obsoleteKpiMembers);

        return usagePointIdsInKpiMembers();
    }

    private Map<Long, DataQualityKpiMember> createDataQualityKpiMembers(Range<Instant> interval) {
        return getUsagePointGroup().getMembers(getClock().instant())
                .stream()
                .map(usagePoint -> Pair.of(usagePoint.getId(), createDataQualityKpiMemberIfPurposeActive(usagePoint, interval)))
                .filter(pair -> pair.getLast().isPresent())
                .collect(Collectors.toMap(Pair::getFirst, pair -> pair.getLast().get()));
    }

    private Optional<DataQualityKpiMember> createDataQualityKpiMemberIfPurposeActive(long usagePointId, Range<Instant> interval) {
        UsagePoint usagePoint = getMeteringService().findUsagePointById(usagePointId).get();
        return createDataQualityKpiMemberIfPurposeActive(usagePoint, interval);
    }

    private Optional<DataQualityKpiMember> createDataQualityKpiMemberIfPurposeActive(UsagePoint usagePoint, Range<Instant> interval) {
        Set<ChannelsContainer> channelContainers = getChannelsContainersForPurpose(usagePoint, getMetrologyPurpose(), interval);
        if (channelContainers.isEmpty()) {
            return Optional.empty();
        }
        KpiBuilder kpiBuilder = getKpiService().newKpi();
        kpiBuilder.interval(getFrequency());
        kpiBuilder.timeZone(usagePoint.getZoneId());

        actualKpiMemberTypes()
                .map(DataQualityKpiMemberType::getName)
                .flatMap(member ->
                        channelContainers.stream()
                                .map(channelContainer -> kpiMemberNameSuffix(usagePoint, channelContainer))
                                .map(suffix -> member.toUpperCase() + DataQualityKpiMember.KPIMEMBERNAME_SEPARATOR + suffix)
                )
                .forEach(member -> kpiBuilder.member().named(member).add());

        DataQualityKpiMemberImpl dataQualityKpiMember = DataQualityKpiMemberImpl.from(getDataModel(), this, kpiBuilder.create());
        getKpiMembers().add(dataQualityKpiMember);
        return Optional.of(dataQualityKpiMember);
    }

    private Map<Long, DataQualityKpiMember> usagePointIdsInKpiMembers() {
        return getKpiMembers().stream()
                .collect(Collectors.toMap(this::parseUsagePointIdentifier, Function.identity()));
    }

    private Long parseUsagePointIdentifier(DataQualityKpiMember kpiMember) {
        String targetIdentifier = kpiMember.getTargetIdentifier();
        String[] parts = targetIdentifier.split(KPI_MEMBER_NAME_SUFFIX_SEPARATOR);
        if (parts.length != 2) {
            throw new IllegalStateException("Usage point kpi member identifier invalid," +
                    " expected <usage point id>" + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + "<channels container id>");
        }
        return Long.parseLong(parts[0]);
    }

    private Set<Long> usagePointIdsInGroup() {
        return getUsagePointGroup().getMembers(getClock().instant()).stream()
                .map(HasId::getId).collect(Collectors.toSet());
    }

    private Set<ChannelsContainer> getChannelsContainersForPurpose(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose, Range<Instant> interval) {
        return usagePoint.getEffectiveMetrologyConfigurations(interval)
                .stream()
                .map(effectiveMC -> getChannelsContainerForPurpose(effectiveMC, metrologyPurpose))
                .flatMap(Functions.asStream())
                .collect(Collectors.toSet());
    }

    private Optional<ChannelsContainer> getChannelsContainerForPurpose(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyPurpose purpose) {
        return effectiveMC
                .getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(metrologyContract -> metrologyContract.getMetrologyPurpose().equals(purpose))
                .findAny()
                .flatMap(effectiveMC::getChannelsContainer);
    }

    public static String kpiMemberNameSuffix(UsagePoint usagePoint, ChannelsContainer channelsContainer) {
        return usagePoint.getId() + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + channelsContainer.getId();
    }
}
